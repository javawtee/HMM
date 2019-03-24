import views.MainView;

public class Main {
    public static void main(String[] args){
        //don't take any argument
        if(args.length > 0)
            System.exit(0);

        // run application
        MainView mainView = new MainView();
    }
}
