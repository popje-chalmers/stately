package app;
import java.io.File;
import machine.*;

public class Stately
{
    public static void main(String[] args)
    {
        File f;

        if(args.length == 0)
        {
            f = null;
        }
        else if(args.length == 1)
        {
            f = new File(args[0]);
        }
        else
        {
            throw new RuntimeException("Usage: java -jar Stately.jar [file]");
        }

        new StatelyApp(f);
    }
}
