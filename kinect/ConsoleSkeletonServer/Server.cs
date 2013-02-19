using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading;

namespace ConsoleSkeletonServer
{
    class Server
    {
        private bool isRunning = true;
        public Server()
        {
            while (isRunning)
            {
                Thread.Sleep(100);
            }
        }
    }
}
