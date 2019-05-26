//

import java.net.*;
import java.io.*;

public class ChatClient {

	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage : java ChatClient <username> <server-ip>");
			System.exit(1);
		}
		Socket sock = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		boolean endflag = false;
		try{
			sock = new Socket(args[1], 10001);//args[1], 10001번 포트로 서버에 접속한다.
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));//메세지를 보낸다
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));//메세지를 출력한다
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));//키보드로 입력받기 위한 buffered생성
			// send username.
			pw.println(args[0]);
			pw.flush();
			InputThread it = new InputThread(sock, br);//서버로부터 전달된 문자열을 모니터에 출력
			it.start();
			String line = null;
			while((line = keyboard.readLine()) != null){
				pw.println(line);//키보드로 입력받은 내용을 보내는 것
				pw.flush();
				if(line.equals("/quit")){
					endflag = true;
					break;
				}
			}//키보드로부터 한줄씩 받아 메세지를 보낸다. 
			System.out.println("Connection closed.");
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);
		}finally{
			try{
				if(pw != null)
					pw.close();
			}catch(Exception ex){}
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		} // finally
	} // main
} // class

class InputThread extends Thread{
	private Socket sock = null;
	private BufferedReader br = null;
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;
	}//서버로부터 전달 받은 문자열을 모니터에 출력하는 InputThread 객체를 생성하여 BuffereadReader와 Socket 객체를 인자로 전달 받음
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				System.out.println(line);
			}
		}catch(Exception ex){
		}finally{
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // InputThread
}
