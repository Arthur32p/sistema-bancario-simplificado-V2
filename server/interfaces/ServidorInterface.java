package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServidorInterface extends Remote {
    public byte[] processarOperacao(byte[] request)  throws RemoteException;
    public void sendReply(byte[] reply)  throws RemoteException;
}
