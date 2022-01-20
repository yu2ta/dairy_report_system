package actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import actions.view.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
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
        //指定されたページ数のデータを取得
        int page = getPage();
        List<EmployeeView> employees = service.getPerPage(page);

        long employeeCount = service.countAll();

        putRequestScope(AttributeConst.EMPLOYEE, employees);
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

    //新規登録画面
    public void entryNew() throws ServletException, IOException {

        putRequestScope(AttributeConst.TOKEN, getTokenId());
        putRequestScope(AttributeConst.EMPLOYEE, new EmployeeView());

        forward(ForwardConst.FW_EMP_NEW);
    }
}
