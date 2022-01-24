package actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import actions.view.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import services.ReportService;

public class ReportAction extends ActionBase {

    private ReportService service;

    @Override
    public void process() throws ServletException, IOException {

        service = new ReportService();

        invoke();
        service.close();

    }

    public void index() throws ServletException, IOException {
        //指定されたページ数の一覧画面に表示する日報データ
        int page = getPage();
        List<ReportView> reports = service.getAllPerPage(page);

        //全日報データの件数を取得
        long reportsCount = service.countAll();

        putRequestScope(AttributeConst.REPORTS, reports);
        putRequestScope(AttributeConst.REP_COUNT, reportsCount);
        putRequestScope(AttributeConst.PAGE, page);
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE);

        //フラッシュメッセージがあったときの処理
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        forward(ForwardConst.FW_REP_INDEX);

    }

}
