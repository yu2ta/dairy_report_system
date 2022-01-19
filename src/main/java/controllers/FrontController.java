package controllers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import action.ActionBase;
import action.UnknownAction;
import constants.ForwardConst;

@WebServlet("/")
public class FrontController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public FrontController() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //アクションクラスのインスタンス
        ActionBase action = getAction(request, response);

        //アクションクラスのフィールド設定
        action.init(getServletContext(), request, response);

        //アクションクラスの処理呼び出し
        action.process();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ActionBase getAction(HttpServletRequest request, HttpServletResponse response) {
        Class type = null;
        ActionBase action = null;

        try {
            //リクエストからパラメータ(action)の値を取得
            String actionString = request.getParameter(ForwardConst.ACT.getValue());

            //該当するActionオブジェクトを作成 (例:リクエストからパラメータ action=Employee の場合、actions.EmployeeActionオブジェクト)
            type = Class.forName(String.format("actions.%sAction", actionString));

            action = (ActionBase) (type.asSubclass(ActionBase.class).getDeclaredConstructor().newInstance());

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SecurityException
                | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
            //エラー処理を行うオブジェクトを作成
            action = new UnknownAction();
        }

        return action;
    }

}
