import java.io.*;
import java.net.*;

public class DownFile_Server extends Thread {
	/*�����������Ӧ���ĵ�content,header,port*/
    private byte[] content;
	private byte[] header;
	private int port= 80;
	
	/*DownFile_Server�Ĺ��췽��*/
	public DownFile_Server(byte[] data, String encoding, String MIMEType, int port) throws UnsupportedEncodingException
	{
		this.content= data;
		this.port= port;
		String header= "HTTP/1.0 200 OK\r\n"+
		"Server: OneFile 1.0\r\n"+
		"Content-length: "+this.content.length+"\r\n"+
		"Content-type: "+MIMEType+"\r\n\r\n";
		this.header= header.getBytes("ASCII");
	}
	/*DownFile_Server��String data���byte[] content��˽�й��췽��*/
	private DownFile_Server(String data, String encoding, String MIMEType, int port) throws UnsupportedEncodingException
	{
		this(data.getBytes(encoding), encoding, MIMEType, port);
	}
	
	/*ʵ���߳�run��������ͻ��˽������ӣ������ݿͻ��˵�����������(ʵ��ֻ�ܺ��ӿͻ�����������ݣ���ֻ���͹̶�����)*/
	public void run(){
		try{
			/*����������׽���server��������������ڵĶ˿�*/
			ServerSocket server= new ServerSocket(this.port);
			System.out.println("Accepting connections on port "+server.getLocalPort());
			System.out.print("�ȴ��û�����...");
			
            /*���ϼ����Ƿ��пͻ�����������*/
            while(true){
            	Socket connection= null;
            	try{
            		/*��ͻ��˽������Ӳ������������*/
            		connection= server.accept();
            		OutputStream out=new BufferedOutputStream(connection.getOutputStream());  
                    InputStream in=new BufferedInputStream(connection.getInputStream());  
                    /*������־*/
                    if(connection!=null){
                    	System.out.println();
                        System.out.println("���û���������");
                    }
                    /*���մӿͻ��˷��͹����������ģ�����ȡ���ĵ�������*/
                    StringBuffer request=new StringBuffer();  
                    while (true) {
                        int c=in.read();
                        if (c=='\r'||c=='\n'||c==-1) {  
                            break;
                        }
                        request.append((char)c);
                    }
                    /*������־*/
                    System.out.println("�ͻ��˷��������ĵ��������ǣ�"+request.toString());
                    
                    /*�����⵽��HTTP/1.0���Ժ��Э�飬���չ淶����Ҫ����һ��MIME�ײ�*/
                    if(request.toString().indexOf("HTTP/")!= -1){
                    	out.write(this.header);
                    }
                    
                    /*��������Ԥ���õ����ݸ��ͻ���*/
                    out.write(this.content);
                    out.flush();
                    
                    /*�ر����������*/
                    out.close();
                    in.close();
            	}
            	catch(IOException e) {  
                    // TODO: handle exception  
                }
            	finally{
                    if (connection!=null) {
                    	/*�ر�����*/
                        connection.close();
                    }
                }
            }
		}
		catch(IOException e)
		{
			System.err.println("Could not start server. Port Occupied");
		}
	}
	
	public static void main(String args[])
	{
		try{
			/*�������ж�����Ҫ�����ͻ��˵��ļ�������ȷ��DownFile���췽���е�MIMEType����*/
			String contentType= "text/plain";
			if(args[0].endsWith(".html")||args[0].endsWith(".htm")){
				contentType= "text/html";
			}
			
			/*�������ļ����뵽�ڴ�data�ֽ������У���ȷ��DownFile���췽���е�MIMEType����*/
			FileInputStream in= new FileInputStream(args[0]);
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			int b;
			while((b=in.read())!=-1){
				out.write(b);
			}
			byte[] data= out.toByteArray();
			
			/*�������ж����������ݵĶ˿ڣ���ȷ��DownFile���췽���е�port����*/
			int port= 80;
			try{
				port=Integer.parseInt(args[1]);  
                if (port<1||port>65535) {  
                    port=80;
                }
			}
			catch(Exception e){
				port=80;
			}
			
			/*�������ж������ݵı��뷽ʽ����ȷ��DownFile���췽���е�encoding����*/
			String encoding="ASCII";
            if (args.length>2) {
                encoding=args[2];
            }
            
            /*����DownFile_Server�߳�ʵ���������������ṩ�ı���*/
            Thread t=new DownFile_Server(data, encoding, contentType, port);  
            t.start();
            
            /*�ر�������in*/
            in.close();
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
	}
}
