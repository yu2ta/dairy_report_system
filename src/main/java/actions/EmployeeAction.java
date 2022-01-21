package actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import actions.view.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import constants.PropertyConst;
import services.EmployeeService;

//従業員に関わる処理
public class EmployeeAction extends ActionBase {
    private EmployeeService service;

    //メソッド実行
    @Override
    public void process() throws ServletException, IOException {
        service = new EmployeeService();

        invoke();

        service.close();
    }

    //一覧画面を表示
    public void index() throws ServletException, IOException {
        if (checkAdmin()) {
            //指定されたページ数のデータを取得
            int page = getPage();
            List<EmployeeView> employees = service.getPerPage(page);

            long employeeCount = service.countAll();

            putRequestScope(AttributeConst.EMPLOYEES, employees);
            putRequestScope(AttributeConst.EMP_COUNT, employeeCount);
            putRequestScope(AttributeConst.PAGE, page);
            putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE);

            String flush = getSessionScope(AttributeConst.FLUSH);
            if (flush != null) {
                putRequestScope(AttributeConst.FLUSH, flush);
                removeSessionScope(AttributeConst.FLUSH);
            }

            forward(ForwardConst.FW_EMP_INDEX);
        }
    }

    //新規登録画面
    public void entryNew() throws ServletException, IOException {
        if (checkAdmin()) {
            putRequestScope(AttributeConst.TOKEN, getTokenId());
            putRequestScope(AttributeConst.EMPLOYEE, new EmployeeView());

            forward(ForwardConst.FW_EMP_NEW);
        }
    }

    //従業員を新規作成
    public void create() throws ServletException, IOException {
        if (checkAdmin()) {
            //CSRFの確認
            if (checkToken()) {
                EmployeeView ev = new EmployeeView(
                        null,
                        getRequestParam(AttributeConst.EMP_CODE),
                        getRequestParam(AttributeConst.EMP_NAME),
                        getRequestParam(AttributeConst.EMP_PASS),
                        toNumber(getRequestParam(AttributeConst.EMP_ADMIN_FLG)), null, null,
                        AttributeConst.DEL_FLAG_FALSE.getIntegerValue());

                String pepper = getContextScope(PropertyConst.PEPPER);
                List<String> errors = service.create(ev, pepper);

                //エラーが返ってきたとき
                if (errors.size() > 0) {
                    putRequestScope(AttributeConst.TOKEN, getTokenId());
                    putRequestScope(AttributeConst.EMPLOYEE, ev);
                    putRequestScope(AttributeConst.ERR, errors);

                    //新規登録画面を再表示してFLUSH
                    forward(ForwardConst.FW_EMP_NEW);
                } else {
                    putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());
                    redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
                }

            }
        }

    }

    //従業員詳細情報を表示
    public void show() throws ServletException, IOException {

        if (checkAdmin()) {
            EmployeeView ev = service.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID)));
            if (ev == null || ev.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()) {

                forward(ForwardConst.FW_ERR_UNKNOWN);
                return;
            }

            putRequestScope(AttributeConst.EMPLOYEE, ev);

            forward(ForwardConst.FW_EMP_SHOW);
        }
    }

    //従業員情報を編集
    public void edit() throws ServletException, IOException {
        if (checkAdmin()) {
            EmployeeView ev = service.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID)));

            if (ev == null || ev.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()) {

                //データが取得できなかった、または論理削除されている場合はエラー画面を表示
                forward(ForwardConst.FW_ERR_UNKNOWN);
                return;
            }
            putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
            putRequestScope(AttributeConst.EMPLOYEE, ev); //取得した従業員情報

            //編集画面を表示する
            forward(ForwardConst.FW_EMP_EDIT);
        }

    }

    //従業員情報を更新
    public void update() throws ServletException, IOException {
        if (checkAdmin()) {
            //CSRF対策 tokenのチェック
            if (checkToken()) {
                //パラメータの値を元に従業員情報のインスタンスを作成する
                EmployeeView ev = new EmployeeView(
                        toNumber(getRequestParam(AttributeConst.EMP_ID)),
                        getRequestParam(AttributeConst.EMP_CODE),
                        getRequestParam(AttributeConst.EMP_NAME),
                        getRequestParam(AttributeConst.EMP_PASS),
                        toNumber(getRequestParam(AttributeConst.EMP_ADMIN_FLG)),
                        null,
                        null,
                        AttributeConst.DEL_FLAG_FALSE.getIntegerValue());

                //アプリケーションスコープからpepper文字列を取得
                String pepper = getContextScope(PropertyConst.PEPPER);

                //従業員情報更新
                List<String> errors = service.update(ev, pepper);

                if (errors.size() > 0) {
                    //更新中にエラーが発生した場合

                    putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                    putRequestScope(AttributeConst.EMPLOYEE, ev); //入力された従業員情報
                    putRequestScope(AttributeConst.ERR, errors); //エラーのリスト

                    //編集画面を再表示
                    forward(ForwardConst.FW_EMP_EDIT);
                } else {
                    //更新中にエラーがなかった場合

                    //セッションに更新完了のフラッシュメッセージを設定
                    putSessionScope(AttributeConst.FLUSH, MessageConst.I_UPDATED.getMessage());

                    //一覧画面にリダイレクト
                    redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
                }
            }
        }
    }

    public void destroy() throws ServletException, IOException {
        if (checkAdmin()) {
            if (checkToken()) {
                service.destroy(toNumber(getRequestParam(AttributeConst.EMP_ID)));
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_DELETED.getMessage());

                redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
            }
        }
    }

    private boolean checkAdmin() throws ServletException, IOException {

        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        if (ev.getAdminFlag() != AttributeConst.ROLE_ADMIN.getIntegerValue()) {
            forward(ForwardConst.FW_ERR_UNKNOWN);
            return false;
        } else {
            return true;
        }
    }
}
