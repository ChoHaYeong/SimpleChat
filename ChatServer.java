//https://github.com/ChoHaYeong/SimpleChat.git

import java.net.*;
import java.io.*;
import java.util.*;

//현재 접속한 사용자 목록 보기 기능
//->id를 한줄씩 읽어오는 것을 저장해보자
//-->send_userlist()라는 method를 생성하자
//---> '/userlist' 를 보내면 접속한 사용자들의 id 및 총 사용자 수를 보여준
//자신이 보낸 채팅 문장은 자신에게는 나타나지 않도록
//금지어 경고기능  --> 얘는 그냥 기존의 함수 안에서 구현해주면 되는걸까?
//리스트를 만들어서 금지어 목록을 만들어준다.

public class ChatServer {//ChatServer라는 객체는 누가 어디서 만들까? --> 객체가 없음(만들어주지 않았잖아)

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);//클라이언트의 요청을 받기 위한 준비를 한다. 10001번을 port로 설정해 서버를 생성한다. -> 다른 서버와 연결하기 위한것
			System.out.println("Waiting connection...");
			HashMap<String, PrintWriter> hm = new HashMap<String, PrintWriter>(); // thread간의 정보 공유를 위한 객체선언  // 실체가있어
			//다 똑같은 hm을 쓰게돼, 공유되고 있는 셈이지
			//공유되는건 다 synchronized시켜라! 줄을 서라고해, synchronized를 사용한다.
			//key는 id, value는 말
			while(true){
				Socket sock = server.accept();//클라이언트의 요청을 받아들인다. 클라이언트의 소켓을 생성 //여기서 waiting이 발생한다.
				ChatThread chatthread = new ChatThread(sock, hm); //(sock, hm, username)이렇게 하면 client로부터 받아오는게 늦어진다면, 너무 늦어진다.
				//서버프로그램의 thread인  chatthread생성
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

//굳이 앞뒤 순서가 필요 없는 일들이 있을 때 분리해서 동시에 처리하고 싶을 때 thread
//multi가 프로그램안에서 가능하도록 한것 (따로따로 돌아가게끔)

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap <String, PrintWriter>hm;
	private boolean initFlag = false;
	
	public ChatThread(Socket sock, HashMap<String, PrintWriter> hm){
		this.sock = sock;
		this.hm = hm; // 생성자 파라미터로 받아온 hm를 자기꺼라고 받아와, 근데 자기가만든거 아니고 가져다가 자기것처럼  쓰게돼
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));//클라이언트에게 데이터전달해주는 출력 스트림
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));//클라이언트로부터 데이터를 받는다
			id = br.readLine(); //문자열 한줄씩 읽기
			broadcast(id + " entered."); // 같이 채팅하는 다른 유저들에게 알린다.
			System.out.println("[Server] User (" + id + ") entered."); 
			synchronized(hm){
				hm.put(this.id, pw);//id랑 pw를 hashmap에넣는다.  
			}
			initFlag = true;//뭐 하려고했던거 같은데 마무리가 되지 않아써
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
					break; //"/quit"를 입력했을 때 반복문을 종료시킨다.
				if(line.equals("/userlist"))
					send_userlist(line); //"/userlist"를 입력하면 send_userlist를 실행한다. 
				//send_userlist()에서는 사용자의 id와 
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else
					broadcast(id + " : " + line);// 위의 어떤 조건도 만족하지 않는다면 채팅방에 문자를 출력하낟.
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){//이거 없을 때 여러사람이 접속해서 동시에 사용하려고하면 충돌된다.
				hm.remove(id);//해시맵에 등록된 id삭제
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	
	public void sendmsg(String msg){//귓속말을 보내는 constructor?
		int start = msg.indexOf(" ") +1; // " "이 위치하는 번호에 1을 더해서 " " 다음이 시작점임을 알린다.
		int end = msg.indexOf(" ", start);//그 다음 " "가 나오는 곳의 위치번호를 통해 끝을 알린다.
		if(end != -1){
			String to = msg.substring(start, end); // 처음과 끝 사이의 문자를 출력한다.
			String msg2 = msg.substring(end+1);// 끝번호 다음부터 메세지가 입력되니까, 그 다음부터 끝까지의 문자열을 msg2에 저장한다.
			Object obj = hm.get(to);// to에 대한 value값을 넣는다. to가 key값과 value값을 가지고 있어??
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj; // 이 값을 pw에 저장
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){ 
		//자기 자신이 보낸 문장을 자기자신의 id만 제외하고 모두에게 보낸다
		//if문을 돌려서 자기자신이 아니면 msg를 출력해준다.
		//자기자신이 아니라는것을 어떻게 표현할가..
		//PrintWriter pw2 = hm.get(id);
		synchronized(hm){
			Collection collection = hm.values(); //values가 메시지 아닌가..? senduserlist에서는 key를 통해서 실행해줘야할거같은데
			Iterator iter = collection.iterator(); // 해시맵에 저장되어있는 모든 사람들에게 메세지를 전달하는 것이다.
			//iterator가 하는 일이 모든 컬렉션클래스의 데이터를 읽는 일을 한다. collection안의 데이터를 ㅇ읽는다. 
			while(iter.hasNext()){//읽어올 데이터가 남아있는지 확인하는 method *false(읽어올 데이터가 없을때) 멈춘다.
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
		
			//해당 사용자에게만 경고메세지가 보내지도록 한다.
		return 0;
	}
		/*
		 * 문자열에 badwords의 문자가 있으면 다른사람에게는 전송하지 않는다.
		 *badwords가 출력되지 않고 해당 사용자에게만 경고메세지가 보내지도록 한다.
		 *
		 *--run함수에서 할일---
		 *문자열에 badwords가 있으면
		 *다른사람에게 전송하지 않는 것을 어떻게 할거야?? --> send_warning을 실행시킨다.(run함수에서실행)
		 *---send_warning에서 할일 ------
		 *문자열에 badwords가 있다--> contain함수를 사용
		 *badwords가 출력되지 않는다 --> broadcast를 금지시켜야하나?? 
		 *해당 사용자에게만 경고메세지가 보내지도록 한다 --> 귓속말로한다.
		 *다른사람에게 전송하지 않는거랑 badwords가 출력되지 않는건 똑같잖아...
		 *badwords문자가 있으면 다른사람에게는 출력하지 않고 해당 사용자에게만 경고메세지가 보내지도록 한다. continue사용해서하기
		*/
	
	public void send_userlist(String msg) {
		//braodcast에서 모든 사용자들에게 어떻게 보냈는지 그거를 생각해봐
		//걔는 모든 사용자를 알고 있잖아
		//그럼 그거랑 똑같은 맥락으로 userlist의 id들도 다 알고있을거잖아,
		//그러니까 여기서 그 id를 보내주고, count를 통해서 몇명인지도 같이 
		int count=0;
		
		synchronized(hm){
			Collection collection = hm.keySet(); //values가 메시지 아닌가..? senduserlist에서는 key를 통해서 실행해줘야할거같은데
			Iterator iter = collection.iterator(); // 해시맵에 저장되어있는 모든 사람들에게 메세지를 전달하는 것이다.
			//iterator가 하는 일이 모든 컬렉션클래스의 데이터를 읽는 일을 한다. collection안의 데이터를 ㅇ읽는다. 
			PrintWriter pw2 = (PrintWriter) hm.get(id);
			while(iter.hasNext()){//읽어올 데이터가 남아있는지 확인하는 method *false(읽어올 데이터가 없을때) 멈춘다.
				String id2 = (String)iter.next(); //next는 공백은 무시하고 문자만 입력
				pw2.println(id2);
				pw2.flush();
				count++;
			}
			pw2.println("total user :" +count);
			pw2.flush();
		}
	}
}
