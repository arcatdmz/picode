using Thrift;
using Thrift.Transport;
using Thrift.Server;
using Jp.Digitalmuseum.Kinect;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ConsoleSkeletonServer
{
    class Program
    {
        static void Main(string[] args)
        {
            // Get options.
            int port = Constants.SERVER_DEFAULT_PORT;
            for (int i = 0; i < args.Length; i++)
            {
                if (args[i] == "-help")
                {
                    Console.WriteLine("usage: ConsoleKinectServer.exe -port PORT_NUMBER");
                    return;
                }
                else if (i < args.Length - 1 && args[i] == "-port")
                {
                    int.TryParse(args[++i], out port);
                }
            }

            // Launch the server.
            //try
            //{
                KinectServiceHandler handler = new KinectServiceHandler();
                KinectService.Processor processor = new KinectService.Processor(handler);
                TServerTransport serverTransport = new TServerSocket(port);
                TServer server = new TSimpleServer(processor, serverTransport);
                handler.Shutdown = server.Stop;
                Console.WriteLine("Starting the server...");
                server.Serve();
            //}
            //catch (Exception e) {
            //    Console.WriteLine(e.StackTrace);
            //}
            Console.WriteLine("Done.");
        }
    }
}
