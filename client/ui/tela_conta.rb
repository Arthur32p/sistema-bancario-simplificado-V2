# tela_conta.rb
require 'pastel'
require 'tty-prompt'
require_relative 'banner'
require_relative '../session'

module UI
  class TelaConta
    MENU_BASE = [
      { label: "Depositar",          key: :depositar   },
      { label: "Sacar",              key: :sacar       },
      { label: "Transferir",         key: :transferir  },
      { label: "Ver Extrato",        key: :extrato     },
      { label: "Sair (Logout)",      key: :sair        },
    ].freeze

    def initialize
      @prompt = TTY::Prompt.new(interrupt: :exit)
    end

    # ── Tela principal ─────────────────────────────────────────────────────────
    def exibir(conta)
      cursor = TTY::Cursor

      Session.notificacao_callback = ->(msg) {
        Thread.new do
          $stdout.print cursor.save
          $stdout.print cursor.move_to(0, _linha_notif)
          $stdout.print "\e[2K"
          $stdout.print UI.margem + UI.secundario("📢 #{msg}")
          $stdout.print cursor.restore
          $stdout.flush
        end
      }

      loop do
        _renderizar_dashboard(conta)

        opcoes = _opcoes_menu(conta)
        opcao  = @prompt.select(
          UI.margem + UI.secundario("O que deseja fazer?"),
          opcoes,
          cycle: true,
          per_page: opcoes.size
        )

        if opcao == :sair
          Session.logout
          break
        end

        _despachar(opcao, conta)
      end
    ensure
      Session.notificacao_callback = nil
    end

    private

    def _linha_notif
      26
    end

    # ── Dashboard ──────────────────────────────────────────────────────────────
    def _renderizar_dashboard(conta)
      UI.cabecalho
      UI.titulo_secao("MINHA CONTA")

      UI.label_secao("TITULAR")
      UI.campo("Nome",  UI.primario(conta.titular.nome))
      UI.campo("CPF",   UI.secundario(conta.titular.cpf))
      UI.espaco

      UI.label_secao("MOVIMENTAÇÃO")
      UI.campo("Número", conta.numero.to_s)
      UI.campo("Tipo",   _badge_tipo(conta))
      UI.espaco

      # Saldo em destaque — principal informação da tela
      _bloco_saldo(conta)
      UI.espaco
      UI.linha_fina
      UI.espaco
    end

    def _bloco_saldo(conta)
      m = UI.margem
      puts m + UI.secundario("  SALDO DISPONÍVEL".ljust(UI::LARGURA_UI))
      puts m + UI.valor_pos("  R$ #{'%.2f' % conta.saldo}".ljust(UI::LARGURA_UI))

      if conta.corrente?
        puts m + UI.secundario("  Limite: R$ #{'%.2f' % conta.limite}")
      else
        puts m + UI.secundario("  Rendimento: #{'%.3f' % (conta.rendimento * 100)}% a.m.")
      end
    end

    def _badge_tipo(conta)
      if conta.corrente?
        UI.badge("CORRENTE", cor: :blue)
      else
        UI.badge("POUPANÇA", cor: :green)
      end
    end

    def _opcoes_menu(conta)
      extras = []
      extras << { name: "Pagar Boleto / Conta", value: :pagar }    if conta.corrente?
      extras << { name: "Projetar Rendimento",  value: :projetar }  if conta.poupanca?

      base = MENU_BASE.map { |o| { name: o[:label], value: o[:key] } }
      # Injeta extras antes de "Ver Extrato"
      idx = base.index { |o| o[:value] == :extrato } || -1
      base.insert(idx, *extras)
    end

    # ── servidor.Dispatcher ─────────────────────────────────────────────────────────────
    def _despachar(opcao, conta)
      case opcao
      when :depositar   then _depositar(conta)
      when :sacar       then _sacar(conta)
      when :transferir  then _transferir(conta)
      when :pagar       then _pagar(conta)
      when :projetar    then _projetar(conta)
      when :extrato     then _extrato(conta)
      end
    end

    # ── Operações ──────────────────────────────────────────────────────────────
    def _depositar(conta)
      valor = _pedir_valor("Valor do depósito")
      return unless valor

      _com_conexao do |conn|
        resultado = conn.depositar(conta.numero, valor)
        if resultado == :ok
          conta.saldo += valor
          UI.sucesso(nil, "Depósito de #{_fmt(valor)} realizado com sucesso.")
        else
          UI.erro(nil, "Não foi possível realizar o depósito.")
        end
      end
      sleep 2
    end

    def _sacar(conta)
      valor = _pedir_valor("Valor do saque")
      return unless valor

      _com_conexao do |conn|
        case conn.sacar(conta.numero, valor)
        when :ok
          conta.saldo -= valor
          UI.sucesso(nil, "Saque de #{_fmt(valor)} efetuado.")
        when :saldo_insuficiente
          UI.erro(nil, "Saldo ou limite insuficiente.")
        else
          UI.erro(nil, "Falha ao realizar o saque.")
        end
      end
      sleep 2
    end

    def _transferir(conta)
      destino = @prompt.ask(UI.secundario("  Conta destino     : "), convert: :integer, required: true)
      valor   = _pedir_valor("Valor da transferência")
      return unless valor

      if destino == conta.numero
        UI.erro(nil, "Não é possível transferir para a própria conta.")
        sleep 2
        return
      end

      _com_conexao do |conn|
        resultado = conn.transferir(conta.numero, destino, valor)
        case resultado
        when :destino_invalido
          UI.erro(nil, "Conta destino (#{destino}) não encontrada.")
        when :saldo_insuficiente
          UI.erro(nil, "Saldo ou limite insuficiente para a transferência.")
        when :erro
          UI.erro(nil, "Falha ao realizar a transferência.")
        else
          conta.saldo -= valor
          UI.sucesso(nil, "Transferência de #{_fmt(valor)} para #{resultado[:nome_destino]} realizada.")
        end
      end
      sleep 4
    end

    def _pagar(conta)
      descricao = @prompt.ask(UI.secundario("  Descrição         : "), required: true)
      valor     = _pedir_valor("Valor do pagamento")
      return unless valor

      _com_conexao do |conn|
        case conn.pagar(conta.numero, valor, descricao)
        when :ok
          conta.saldo -= valor
          UI.sucesso(nil, "Pagamento de #{_fmt(valor)} — #{descricao} — realizado.")
        when :saldo_insuficiente
          UI.erro(nil, "Saldo ou limite insuficiente para o pagamento.")
        else
          UI.erro(nil, "Falha ao processar o pagamento.")
        end
      end
      sleep 2
    end

    def _projetar(conta)
      meses = @prompt.ask(UI.secundario("  Número de meses   : "), convert: :integer, required: true)
      return UI.erro(nil, "Número de meses inválido.") unless meses&.positive?

      _com_conexao do |conn|
        resultado = conn.projetar_rendimento(conta.numero, meses)
        if resultado[:status] == :ok
          UI.espaco
          UI.label_secao("PROJEÇÃO DE RENDIMENTO")
          UI.campo("Período",         "#{meses} meses")
          UI.campo("Saldo atual",     _fmt(conta.saldo))
          UI.campo("Saldo projetado", UI.valor_pos("R$ #{'%.2f' % resultado[:valor]}"))
          UI.espaco
          @prompt.keypress(UI.margem + UI.secundario("  Pressione qualquer tecla para continuar..."))
        else
          UI.erro(nil, "Não foi possível calcular a projeção.")
          sleep 2
        end
      end
    end

    def _extrato(conta)
      _com_conexao do |conn|
        linhas = conn.extrato(conta.numero)

        if linhas.nil?
          UI.erro(nil, "Conta não encontrada.")
          sleep 2
          next
        end

        UI.cabecalho
        UI.titulo_secao("EXTRATO DE MOVIMENTAÇÕES")
        UI.label_secao("Conta #{conta.numero}  •  #{conta.titular.nome}")

        if linhas.empty?
          UI.espaco
          UI.info("Nenhuma movimentação registrada.")
        else
          linhas.each { |l| puts UI.margem + "  " + l }
        end

        UI.espaco
        UI.linha_fina
        UI.espaco
        @prompt.keypress(UI.margem + UI.secundario("  Pressione qualquer tecla para voltar..."))
      end
    end

    # ── Helpers ────────────────────────────────────────────────────────────────

    def _pedir_valor(label)
      valor = @prompt.ask(UI.secundario("  #{label.ljust(22)}: R$ "), convert: :float, required: true)
      if valor.nil? || valor <= 0
        UI.erro(nil, "Valor inválido.")
        sleep 1
        return nil
      end
      valor
    end

    # Garante que a conexão sempre fecha, mesmo com exceção
    def _com_conexao
      conn = Connection.new
      yield conn
    rescue StandardError => e
      UI.erro(nil, "Erro de comunicação: #{e.message}")
      sleep 2
    ensure
      conn&.close
    end

    def _fmt(valor)
      UI.primario("R$ #{'%.2f' % valor}")
    end
  end
end