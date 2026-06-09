package shared.routers;

import base.Params;
import base.SubRouter;
import my_base.App;
import team.control.GameController;

public class BilliardRouter implements SubRouter {

    private final GameController controller;

    public BilliardRouter(GameController controller) {
        this.controller = controller;
    }

    @Override
    public Object route(String subPath, Params p) {
        switch (subPath) {
            case "/start":
                controller.startScenario();
                return null;

            case "/ball/strike":
                double forceX = p.getDouble(0);
                double forceY = p.getDouble(1);
                controller.strikeCueBall(forceX, forceY);
                return null;
                
            case "/game/reset":
                // למימוש עתידי מכפתור ה-Reset
                return null;

            default:
                throw new RuntimeException("Unknown game route: " + subPath);
        }
    }
}