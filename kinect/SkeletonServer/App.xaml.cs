using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Windows;

namespace SkeletonServer
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App : Application
    {
        private void Application_Startup(object sender, StartupEventArgs e)
        {
            MainWindow mainWindow = new MainWindow(e);

            // Check command line parameters.
            foreach (string s in e.Args)
            {
                if (s == "/AutomatedServer")
                {
                    // return;
                }
            }
            mainWindow.Show();
        }
    }
}
