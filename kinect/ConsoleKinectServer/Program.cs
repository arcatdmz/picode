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
            try
            {
                KinectServiceHandler handler = new KinectServiceHandler();
                KinectService.Processor processor = new KinectService.Processor(handler);
                TServerTransport serverTransport = new TServerSocket(9090);
                TServer server = new TSimpleServer(processor, serverTransport);
                handler.Shutdown = server.Stop;
                Console.WriteLine("Starting the server...");
                server.Serve();
            }
            catch (Exception e) {
                Console.WriteLine(e.StackTrace);
                throw e;
            }
            Console.WriteLine("done.");
        }
    }
}
