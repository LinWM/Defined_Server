import java.io.*;
import java.net.*;
import java.util.*;

public class DownFile_Server extends Thread {
	int m_id;
	
	/* ************************************************
	 * DownFile_Server�Ĺ��췽��
	 * ������int id����ʶ�߳�
	 * ���ܣ�����DownFile_S�߳�ʵ��
	 * ************************************************/
	public DownFile_Server(int id) {
		m_id= id;
	}
	
	/* *********************************************************
	 * DownFile_S�̵߳�run��Ա����ģ��
	 * ��������
	 * ����ֵ��void
	 * ���ܣ�ʵ���߳�run��������ͻ��˽������ӣ������ݿͻ��˵�����������
	 * *********************************************************/
	public void run(){
		try{
			/*����������׽���server��������������ڵĶ˿�*/
			ServerSocket server= new ServerSocket(8080);
			System.out.println("Accepting connections on port "+server.getLocalPort());
			
            /*���ϼ����Ƿ��пͻ�����������*/
            while(true){
            	Socket connection= null;
            	try{
            	   System.out.println("");
            		System.out.print("�ȴ��ͻ�������...");
            		
            		/*��ͻ��˽������Ӳ������������*/
            		connection= server.accept();
            		OutputStream out=new BufferedOutputStream(connection.getOutputStream());
                    InputStream in=new BufferedInputStream(connection.getInputStream());
                    
                    /*������־*/
                    if(connection!=null){
                    	System.out.println();
                        System.out.println("------------------------------��ͻ��˽������ӣ�����ʼ---------------");
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
                    
                    /*����send������ͻ��˷�������*/
                    send(request.toString(), out);
                    
                    /*�ر����������*/
                    out.close();
                    in.close();
            	}
            	catch(IOException e) {  
                    System.err.println("�����������������");
                }
            	finally{
                    if (connection!=null) {
                    	   /*�ر�����*/
                        connection.close();
                        System.out.println("------------------------------��ͻ��˶Ͽ����ӣ��������---------------");
                    }
                }
            }
		}
		catch(IOException e)
		{
			System.err.println("Could not start server. Port Occupied");
		}
	}
	
	/* ****************************************************
	 * send��Ա����ģ��
	 * ������String request���ͻ��˷����������ĵ�������
	 *      OutputStream out����ͻ��˽������Ӷ����������������
	 * ����ֵ��void
	 * ���ܣ�����sendʵ����ͻ��˷������������Դ
	 * ****************************************************/
	public void send(String request, OutputStream out) {
		try{
			/*���÷�������վ��Ĭ����ҳ*/
			String indexFileName="index.html";
			/*���������õ��ı���*/
			String method;       //�����з���
			String name;         //�������Դ��
			String MIMEType;     //�������Դ����
			String version="";   //�����Э��汾
			String header;       //����˷����ͻ��˵���Ӧ����(HTTPЭ��Ҫ�����Ӧ����)
			byte[] content;      //����˷����ͻ��˵���������
			
			StringTokenizer st=new StringTokenizer(request);
			
			/*������������ȡ������������method*/
			method= st.nextToken();
			
         /*������־*/
         System.out.println("�ͻ��˷��������ĵ��������ǣ�"+request.toString());
         
			/*˵��������ֻ�ܴ���GET����������*/
			if(method.compareTo("GET")==0){
				/*������������ȡ��Դ�ļ�����������name*/
				name= st.nextToken();
				if (name.endsWith("/")){
					name+= indexFileName;
				}
				
				/*��name�л�ȡ��Դ���ͣ�������MIMEType*/
				MIMEType=guessContentTypeFromName(name);
				
				/*������������ȡЭ��汾��������version*/
				if(st.hasMoreTokens()){
					version= st.nextToken();
				}
				
				try{
					/*��������Ϊname���ļ�����content�ֽ�������*/
					FileInputStream in= new FileInputStream(name.substring(1, name.length()));
					ByteArrayOutputStream outBytes=new ByteArrayOutputStream();
					int b;
					while((b=in.read())!=-1){
						outBytes.write(b);
					}
					content= outBytes.toByteArray();
					in.close();
					
					/*�����⵽��HTTP/1.0���Ժ��Э�飬���չ淶����Ҫ����һ��MIME�ײ�*/
		            if(version.startsWith("HTTP")){
		            	Date now=new Date();
		            	header= "HTTP/1.0 200 OK\r\n"+
		            			"Date: "+now+"\r\n"+
		            			"Server: DownFile_S 1.0\r\n"+
		    					"Content-length: "+content.length+"\r\n"+
		    					"Content-type: "+MIMEType+"\r\n\r\n";
		            	out.write(header.getBytes("ASCII"));
		            }
		            

		            /*�����Դcontent���͸��ͻ���*/
		            out.write(content);
		            out.flush();
				}
				catch (IOException e){
					if (version.startsWith("HTTP")) {
						Date now=new Date();
		            	header= "HTTP/1.0 404 File Not Found\r\n"+
		            			"Date: "+now+"\r\n"+
		            			"Server: DownFile_S 1.0\r\n"+
		    					"Content-Type: text/html\r\n\r\n";
		            	out.write(header.getBytes("ASCII"));
					}
					String html="<HTML>\r\n"+
            				"<HEAD><TITLE>File Not Found</TITLE></HRAD>\r\n"+
            				"<BODY>\r\n"+
            				"<H1>HTTP Error 404: File Not Found</H1>"+
            				"</BODY></HTML>\r\n";
					out.write(html.getBytes("ASCII"));
				}
			}
			else{//����GET����ʱ�����д���
				if (version.startsWith("HTTP")) {
					Date now=new Date();
	            	header= "HTTP/1.0 501 Not Implemented\r\n"+
	            			"Date: "+now+"\r\n"+
	            			"Server: DownFile_S 1.0\r\n"+
	    					"Content-Type: text/html\r\n\r\n";
	            	out.write(header.getBytes("ASCII"));
				}
            	String html="<HTML>\r\n"+
            				"<HEAD><TITLE>Not Implemented</TITLE></HRAD>\r\n"+
            				"<BODY>\r\n"+
            				"<H1>HTTP Error 501: Not Implemented</H1>"+
            				"</BODY></HTML>\r\n";
            	out.write(html.getBytes("ASCII"));
			}
		}
		catch (NoSuchElementException e) {//��׽NoSuchElementException�������ͣ������д���
		    /*���ֻ�������Ӷ��ͻ���û�з��������ģ���ô�Ͽ����Ӳ����½������Ӽ���*/
          System.out.println("�ͻ���û�з���������");
		}
		catch (IOException e){
			System.err.println("�������������");
		}
	}
	
	/* ******************************************
	 * guessContentTypeFromName��Ա����ģ��
	 * ������String name���ͻ����������Դ��
	 * ����ֵ����Դ����(String)
	 * ���ܣ�ͨ����Դ��ȷ����Դ����
	 * ******************************************/
	public static String guessContentTypeFromName(String name) {
        if (name.endsWith(".html")||name.endsWith(".htm")) {//html�ĵ�����
            return "text/html";
        }else if (name.endsWith(".txt")||name.endsWith(".java")) {//txt�ĵ�����
            return "text/plain";
        }else if (name.endsWith(".gif")) {//gifͼƬ����
            return "image/gif";
        }else if (name.endsWith(".jpg")||name.endsWith(".jpeg")) {//jpgͼƬ����
            return "image/jpeg";
        }else if (name.endsWith(".png")) {//pngͼƬ����
            return "image/png";
        }else if (name.endsWith(".mp3")) {//mp3��������
            return "audio/x-mpeg";
        }
        else if (name.endsWith(".css")) {//css��ҳ�������
            return "text/css";
        }else if(name.endsWith(".js")) {//js�ű���������
            return "text/javascript";
        }else if (name.endsWith(".class")) {//class�ֽ����ļ�����
            return "application/octet-stream";
        }else {
            return "text/plain";
        }
	}
	
	/* ***************************
	 * main�������ģ��
	 * ������String args[]
	 * ����ֵ��void
	 * ���ܣ������������߳�
	 * ***************************/
	public static void main(String args[]){
		DownFile_Server serverThread= new DownFile_Server(1);
		serverThread.start();
	}
}
