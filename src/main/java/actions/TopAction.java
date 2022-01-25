package actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import actions.view.EmployeeView;
import actions.view.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import services.ReportService;

public class TopAction extends ActionBase {

    private ReportService service;

    @Override
    public void process() throws ServletException, IOException {
        service = new ReportService();
        invoke();
        service.close();
    }

    public void index() throws ServletException, IOException {
        //ログイン中の従業員の情報
        EmployeeView loginEmployee = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //ログイン中の従業員の日報データを一覧に表示する
        int page = getPage();
        List<ReportView> reports = service.getMinePerPage(loginEmployee, page);

        long myReportsCount = service.countAllMine(loginEmployee);

        putRequestScope(AttributeConst.REPORTS, reports); //取得した日報データ
        putRequestScope(AttributeConst.REP_COUNT, myReportsCount); //ログイン中の従業員が作成した日報の数
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        String flush = getSessionScope(AttributeConst.FLUSH);

        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        forward(ForwardConst.FW_TOP_INDEX);
    }
}
