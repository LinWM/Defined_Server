import java.io.*;
import java.net.*;

public class DownFile_Server extends Thread {
	/*定义服务器响应报文的content,header,port*/
    private byte[] content;
	private byte[] header;
	private int port= 80;
	
	/*DownFile_Server的构造方法*/
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
	/*DownFile_Server把String data变成byte[] content的私有构造方法*/
	private DownFile_Server(String data, String encoding, String MIMEType, int port) throws UnsupportedEncodingException
	{
		this(data.getBytes(encoding), encoding, MIMEType, port);
	}
	
	/*实现线程run方法，与客户端建立连接，并根据客户端的请求发送数据(实际只能忽视客户端所需的数据，而只发送固定数据)*/
	public void run(){
		try{
			/*建立服务端套接字server，并输出连接所在的端口*/
			ServerSocket server= new ServerSocket(this.port);
			System.out.println("Accepting connections on port "+server.getLocalPort());
			System.out.print("等待用户连接...");
			
            /*不断监听是否有客户端请求连接*/
            while(true){
            	Socket connection= null;
            	try{
            		/*与客户端建立连接并打开输入输出流*/
            		connection= server.accept();
            		OutputStream out=new BufferedOutputStream(connection.getOutputStream());  
                    InputStream in=new BufferedInputStream(connection.getInputStream());  
                    /*测试日志*/
                    if(connection!=null){
                    	System.out.println();
                        System.out.println("与用户建立连接");
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
                    /*测试日志*/
                    System.out.println("客户端发来请求报文的请求行是："+request.toString());
                    
                    /*如果检测到是HTTP/1.0及以后的协议，按照规范，需要发送一个MIME首部*/
                    if(request.toString().indexOf("HTTP/")!= -1){
                    	out.write(this.header);
                    }
                    
                    /*发送事先预备好的数据给客户端*/
                    out.write(this.content);
                    out.flush();
                    
                    /*关闭输入输出流*/
                    out.close();
                    in.close();
            	}
            	catch(IOException e) {  
                    // TODO: handle exception  
                }
            	finally{
                    if (connection!=null) {
                    	/*关闭连接*/
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
			/*从命令行读到所要传给客户端的文件名，并确定DownFile构造方法中的MIMEType参数*/
			String contentType= "text/plain";
			if(args[0].endsWith(".html")||args[0].endsWith(".htm")){
				contentType= "text/html";
			}
			
			/*将本地文件读入到内存data字节数组中，并确定DownFile构造方法中的MIMEType参数*/
			FileInputStream in= new FileInputStream(args[0]);
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			int b;
			while((b=in.read())!=-1){
				out.write(b);
			}
			byte[] data= out.toByteArray();
			
			/*从命令行读到传送数据的端口，并确定DownFile构造方法中的port参数*/
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
			
			/*从命令行读到数据的编码方式，并确定DownFile构造方法中的encoding参数*/
			String encoding="ASCII";
            if (args.length>2) {
                encoding=args[2];
            }
            
            /*创建DownFile_Server线程实例，参数用上面提供的变量*/
            Thread t=new DownFile_Server(data, encoding, contentType, port);  
            t.start();
            
            /*关闭输入流in*/
            in.close();
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
	}
}
