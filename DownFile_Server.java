import java.io.*;
import java.net.*;
import java.util.*;

public class DownFile_Server extends Thread {
	int m_id;
	
	/* ************************************************
	 * DownFile_Server的构造方法
	 * 参数：int id：标识线程
	 * 功能：创建DownFile_S线程实例
	 * ************************************************/
	public DownFile_Server(int id) {
		m_id= id;
	}
	
	/* *********************************************************
	 * DownFile_S线程的run成员方法模块
	 * 参数：无
	 * 返回值：void
	 * 功能：实现线程run方法，与客户端建立连接，并根据客户端的请求发送数据
	 * *********************************************************/
	public void run(){
		try{
			/*建立服务端套接字server，并输出连接所在的端口*/
			ServerSocket server= new ServerSocket(8080);
			System.out.println("Accepting connections on port "+server.getLocalPort());
			
            /*不断监听是否有客户端请求连接*/
            while(true){
            	Socket connection= null;
            	try{
            	   System.out.println("");
            		System.out.print("等待客户端连接...");
            		
            		/*与客户端建立连接并打开输入输出流*/
            		connection= server.accept();
            		OutputStream out=new BufferedOutputStream(connection.getOutputStream());
                    InputStream in=new BufferedInputStream(connection.getInputStream());
                    
                    /*测试日志*/
                    if(connection!=null){
                    	System.out.println();
                        System.out.println("------------------------------与客户端建立连接，请求开始---------------");
                    }
                    
                    /*接收从客户端发送过来的请求报文，并读取报文的请求行*/
                    StringBuffer request=new StringBuffer();  
                    while (true) {
                        int c=in.read();
                        if (c=='\r'||c=='\n'||c==-1) {  
                            break;
                        }
                        request.append((char)c);
                    }
                    
                    /*调用send方法向客户端发送数据*/
                    send(request.toString(), out);
                    
                    /*关闭输入输出流*/
                    out.close();
                    in.close();
            	}
            	catch(IOException e) {  
                    System.err.println("数据输入输出流出错");
                }
            	finally{
                    if (connection!=null) {
                    	   /*关闭连接*/
                        connection.close();
                        System.out.println("------------------------------与客户端断开连接，请求结束---------------");
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
	 * send成员方法模块
	 * 参数：String request：客户端发来的请求报文的请求行
	 *      OutputStream out：与客户端建立连接而产生的数据输出流
	 * 返回值：void
	 * 功能：方法send实现向客户端发送其所需的资源
	 * ****************************************************/
	public void send(String request, OutputStream out) {
		try{
			/*设置服务器网站的默认网页*/
			String indexFileName="index.html";
			/*声明各种用到的变量*/
			String method;       //请求行方法
			String name;         //请求的资源名
			String MIMEType;     //请求的资源类型
			String version="";   //请求的协议版本
			String header;       //服务端发给客户端的响应报文(HTTP协议要求的响应报文)
			byte[] content;      //服务端发给客户端的数据内容
			
			StringTokenizer st=new StringTokenizer(request);
			
			/*从请求报文中提取方法，并赋给method*/
			method= st.nextToken();
			
         /*测试日志*/
         System.out.println("客户端发来请求报文的请求行是："+request.toString());
         
			/*说明服务器只能处理GET方法的请求*/
			if(method.compareTo("GET")==0){
				/*从请求报文中提取资源文件名，并赋给name*/
				name= st.nextToken();
				if (name.endsWith("/")){
					name+= indexFileName;
				}
				
				/*从name中获取资源类型，并赋给MIMEType*/
				MIMEType=guessContentTypeFromName(name);
				
				/*从请求报文中提取协议版本，并赋给version*/
				if(st.hasMoreTokens()){
					version= st.nextToken();
				}
				
				try{
					/*将本地名为name的文件读到content字节数组中*/
					FileInputStream in= new FileInputStream(name.substring(1, name.length()));
					ByteArrayOutputStream outBytes=new ByteArrayOutputStream();
					int b;
					while((b=in.read())!=-1){
						outBytes.write(b);
					}
					content= outBytes.toByteArray();
					in.close();
					
					/*如果检测到是HTTP/1.0及以后的协议，按照规范，需要发送一个MIME首部*/
		            if(version.startsWith("HTTP")){
		            	Date now=new Date();
		            	header= "HTTP/1.0 200 OK\r\n"+
		            			"Date: "+now+"\r\n"+
		            			"Server: DownFile_S 1.0\r\n"+
		    					"Content-length: "+content.length+"\r\n"+
		    					"Content-type: "+MIMEType+"\r\n\r\n";
		            	out.write(header.getBytes("ASCII"));
		            }
		            

		            /*最后将资源content发送给客户端*/
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
			else{//不是GET方法时，进行处理
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
		catch (NoSuchElementException e) {//捕捉NoSuchElementException错误类型，并进行处理
		    /*如果只建立连接而客户端没有发送请求报文，那么断开连接并重新进行连接监听*/
          System.out.println("客户端没有发送请求报文");
		}
		catch (IOException e){
			System.err.println("数据输出流出错");
		}
	}
	
	/* ******************************************
	 * guessContentTypeFromName成员方法模块
	 * 参数：String name：客户端请求的资源名
	 * 返回值：资源类型(String)
	 * 功能：通过资源名确定资源类型
	 * ******************************************/
	public static String guessContentTypeFromName(String name) {
        if (name.endsWith(".html")||name.endsWith(".htm")) {//html文档类型
            return "text/html";
        }else if (name.endsWith(".txt")||name.endsWith(".java")) {//txt文档类型
            return "text/plain";
        }else if (name.endsWith(".gif")) {//gif图片类型
            return "image/gif";
        }else if (name.endsWith(".jpg")||name.endsWith(".jpeg")) {//jpg图片类型
            return "image/jpeg";
        }else if (name.endsWith(".png")) {//png图片类型
            return "image/png";
        }else if (name.endsWith(".mp3")) {//mp3音乐类型
            return "audio/x-mpeg";
        }
        else if (name.endsWith(".css")) {//css网页框架类型
            return "text/css";
        }else if(name.endsWith(".js")) {//js脚本语言类型
            return "text/javascript";
        }else if (name.endsWith(".class")) {//class字节码文件类型
            return "application/octet-stream";
        }else {
            return "text/plain";
        }
	}
	
	/* ***************************
	 * main函数入口模块
	 * 参数：String args[]
	 * 返回值：void
	 * 功能：创建服务器线程
	 * ***************************/
	public static void main(String args[]){
		DownFile_Server serverThread= new DownFile_Server(1);
		serverThread.start();
	}
}
