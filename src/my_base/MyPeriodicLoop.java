package my_base;

import base.PeriodicLoop;

public class MyPeriodicLoop extends PeriodicLoop {

    private AppContent content = App.content();
    
    @Override
    public void execute() {
        // נותן למחלקת האב לעשות את העבודה שלה
        super.execute();
        
        // בכל פעימה של השעון, מפעילים את הפיזיקה של הביליארד!
        if (content.game() != null) {
            content.game().updatePhysics();
        }
    }
}