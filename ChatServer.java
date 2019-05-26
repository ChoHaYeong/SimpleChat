//https://github.com/ChoHaYeong/SimpleChat.git

import java.net.*;
import java.io.*;
import java.util.*;

//���� ������ ����� ��� ���� ���
//->id�� ���پ� �о���� ���� �����غ���
//-->send_userlist()��� method�� ��������
//---> '/userlist' �� ������ ������ ����ڵ��� id �� �� ����� ���� ������
//�ڽ��� ���� ä�� ������ �ڽſ��Դ� ��Ÿ���� �ʵ���
//������ �����  --> ��� �׳� ������ �Լ� �ȿ��� �������ָ� �Ǵ°ɱ�?
//����Ʈ�� ���� ������ ����� ������ش�.

public class ChatServer {//ChatServer��� ��ü�� ���� ��� �����? --> ��ü�� ����(��������� �ʾ��ݾ�)

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);//Ŭ���̾�Ʈ�� ��û�� �ޱ� ���� �غ� �Ѵ�. 10001���� port�� ������ ������ �����Ѵ�. -> �ٸ� ������ �����ϱ� ���Ѱ�
			System.out.println("Waiting connection...");
			HashMap<String, PrintWriter> hm = new HashMap<String, PrintWriter>(); // thread���� ���� ������ ���� ��ü����  // ��ü���־�
			//�� �Ȱ��� hm�� ���Ե�, �����ǰ� �ִ� ������
			//�����Ǵ°� �� synchronized���Ѷ�! ���� �������, synchronized�� ����Ѵ�.
			//key�� id, value�� ��
			while(true){
				Socket sock = server.accept();//Ŭ���̾�Ʈ�� ��û�� �޾Ƶ��δ�. Ŭ���̾�Ʈ�� ������ ���� //���⼭ waiting�� �߻��Ѵ�.
				ChatThread chatthread = new ChatThread(sock, hm); //(sock, hm, username)�̷��� �ϸ� client�κ��� �޾ƿ��°� �ʾ����ٸ�, �ʹ� �ʾ�����.
				//�������α׷��� thread��  chatthread����
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

//���� �յ� ������ �ʿ� ���� �ϵ��� ���� �� �и��ؼ� ���ÿ� ó���ϰ� ���� �� thread
//multi�� ���α׷��ȿ��� �����ϵ��� �Ѱ� (���ε��� ���ư��Բ�)

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap <String, PrintWriter>hm;
	private boolean initFlag = false;
	
	public ChatThread(Socket sock, HashMap<String, PrintWriter> hm){
		this.sock = sock;
		this.hm = hm; // ������ �Ķ���ͷ� �޾ƿ� hm�� �ڱⲨ��� �޾ƿ�, �ٵ� �ڱⰡ����� �ƴϰ� �����ٰ� �ڱ��ó��  ���Ե�
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));//Ŭ���̾�Ʈ���� �������������ִ� ��� ��Ʈ��
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));//Ŭ���̾�Ʈ�κ��� �����͸� �޴´�
			id = br.readLine(); //���ڿ� ���پ� �б�
			broadcast(id + " entered."); // ���� ä���ϴ� �ٸ� �����鿡�� �˸���.
			System.out.println("[Server] User (" + id + ") entered."); 
			synchronized(hm){
				hm.put(this.id, pw);//id�� pw�� hashmap���ִ´�.  
			}
			initFlag = true;//�� �Ϸ����ߴ��� ������ �������� ���� �ʾƽ�
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // constructor

	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(send_warning(line)==1)
					continue;
				if(line.equals("/quit"))
					break; //"/quit"�� �Է����� �� �ݺ����� �����Ų��.
				if(line.equals("/userlist"))
					send_userlist(line); //"/userlist"�� �Է��ϸ� send_userlist�� �����Ѵ�. 
				//send_userlist()������ ������� id�� 
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else
					broadcast(id + " : " + line);// ���� � ���ǵ� �������� �ʴ´ٸ� ä�ù濡 ���ڸ� ����ϳ�.
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){//�̰� ���� �� ��������� �����ؼ� ���ÿ� ����Ϸ����ϸ� �浹�ȴ�.
				hm.remove(id);//�ؽøʿ� ��ϵ� id����
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	
	public void sendmsg(String msg){//�ӼӸ��� ������ constructor?
		int start = msg.indexOf(" ") +1; // " "�� ��ġ�ϴ� ��ȣ�� 1�� ���ؼ� " " ������ ���������� �˸���.
		int end = msg.indexOf(" ", start);//�� ���� " "�� ������ ���� ��ġ��ȣ�� ���� ���� �˸���.
		if(end != -1){
			String to = msg.substring(start, end); // ó���� �� ������ ���ڸ� ����Ѵ�.
			String msg2 = msg.substring(end+1);// ����ȣ �������� �޼����� �ԷµǴϱ�, �� �������� �������� ���ڿ��� msg2�� �����Ѵ�.
			Object obj = hm.get(to);// to�� ���� value���� �ִ´�. to�� key���� value���� ������ �־�??
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj; // �� ���� pw�� ����
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){ 
		//�ڱ� �ڽ��� ���� ������ �ڱ��ڽ��� id�� �����ϰ� ��ο��� ������
		//if���� ������ �ڱ��ڽ��� �ƴϸ� msg�� ������ش�.
		//�ڱ��ڽ��� �ƴ϶�°��� ��� ǥ���Ұ�..
		//PrintWriter pw2 = hm.get(id);
		synchronized(hm){
			Collection collection = hm.values(); //values�� �޽��� �ƴѰ�..? senduserlist������ key�� ���ؼ� ����������ҰŰ�����
			Iterator iter = collection.iterator(); // �ؽøʿ� ����Ǿ��ִ� ��� ����鿡�� �޼����� �����ϴ� ���̴�.
			//iterator�� �ϴ� ���� ��� �÷���Ŭ������ �����͸� �д� ���� �Ѵ�. collection���� �����͸� ���д´�. 
			while(iter.hasNext()){//�о�� �����Ͱ� �����ִ��� Ȯ���ϴ� method *false(�о�� �����Ͱ� ������) �����.
				PrintWriter pw = (PrintWriter)iter.next();
				if(pw!=hm.get(id)) {
					pw.println(msg);
					pw.flush();
				}
			}
		}
	} // broadcast
	
	public int send_warning(String msg) {
		String[] badwords = {"sial", "bungin", "dakchu", "stupid", "gial"};
		for(String badword: badwords) {
			if(msg.indexOf(badword) > -1) {
				hm.get(id).println("warning!!");
				hm.get(id).flush();
				return 1;
			}
		}
		
			//�ش� ����ڿ��Ը� ���޼����� ���������� �Ѵ�.
		return 0;
	}
		/*
		 * ���ڿ��� badwords�� ���ڰ� ������ �ٸ�������Դ� �������� �ʴ´�.
		 *badwords�� ��µ��� �ʰ� �ش� ����ڿ��Ը� ���޼����� ���������� �Ѵ�.
		 *
		 *--run�Լ����� ����---
		 *���ڿ��� badwords�� ������
		 *�ٸ�������� �������� �ʴ� ���� ��� �Ұž�?? --> send_warning�� �����Ų��.(run�Լ���������)
		 *---send_warning���� ���� ------
		 *���ڿ��� badwords�� �ִ�--> contain�Լ��� ���
		 *badwords�� ��µ��� �ʴ´� --> broadcast�� �������Ѿ��ϳ�?? 
		 *�ش� ����ڿ��Ը� ���޼����� ���������� �Ѵ� --> �ӼӸ����Ѵ�.
		 *�ٸ�������� �������� �ʴ°Ŷ� badwords�� ��µ��� �ʴ°� �Ȱ��ݾ�...
		 *badwords���ڰ� ������ �ٸ�������Դ� ������� �ʰ� �ش� ����ڿ��Ը� ���޼����� ���������� �Ѵ�. continue����ؼ��ϱ�
		*/
	
	public void send_userlist(String msg) {
		//braodcast���� ��� ����ڵ鿡�� ��� ���´��� �װŸ� �����غ�
		//�´� ��� ����ڸ� �˰� ���ݾ�
		//�׷� �װŶ� �Ȱ��� �ƶ����� userlist�� id�鵵 �� �˰��������ݾ�,
		//�׷��ϱ� ���⼭ �� id�� �����ְ�, count�� ���ؼ� ��������� ���� 
		int count=0;
		
		synchronized(hm){
			Collection collection = hm.keySet(); //values�� �޽��� �ƴѰ�..? senduserlist������ key�� ���ؼ� ����������ҰŰ�����
			Iterator iter = collection.iterator(); // �ؽøʿ� ����Ǿ��ִ� ��� ����鿡�� �޼����� �����ϴ� ���̴�.
			//iterator�� �ϴ� ���� ��� �÷���Ŭ������ �����͸� �д� ���� �Ѵ�. collection���� �����͸� ���д´�. 
			PrintWriter pw2 = (PrintWriter) hm.get(id);
			while(iter.hasNext()){//�о�� �����Ͱ� �����ִ��� Ȯ���ϴ� method *false(�о�� �����Ͱ� ������) �����.
				String id2 = (String)iter.next(); //next�� ������ �����ϰ� ���ڸ� �Է�
				pw2.println(id2);
				pw2.flush();
				count++;
			}
			pw2.println("total user :" +count);
			pw2.flush();
		}
	}
}
