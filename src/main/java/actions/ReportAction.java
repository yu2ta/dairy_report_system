package actions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;

import actions.view.EmployeeView;
import actions.view.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
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

    public void entryNew() throws ServletException, IOException {

        putRequestScope(AttributeConst.TOKEN, getTokenId());

        //日付を設定する
        ReportView rv = new ReportView();
        rv.setReportDate(LocalDate.now());
        putRequestScope(AttributeConst.REPORT, rv);

        //新規登録画面を表示
        forward(ForwardConst.FW_REP_NEW);
    }

    public void create() throws ServletException, IOException {
        if (checkToken()) {

            LocalDate day = null;
            if (getRequestParam(AttributeConst.REP_DATE) == null) {
                day = LocalDate.now();
            } else {
                day = LocalDate.parse(getRequestParam(AttributeConst.REP_DATE));
            }

            //セッションスコープからログイン中の従業員情報を取得
            EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

            //パラメータの値から日報情報を作成
            ReportView rv = new ReportView(
                    null,
                    ev,
                    day,
                    getRequestParam(AttributeConst.REP_TITLE),
                    getRequestParam(AttributeConst.REP_CONTENT),
                    null, null);

            List<String> errors = service.create(rv);

            //登録中にエラーがあった場合
            if (errors.size() > 0) {

                putRequestScope(AttributeConst.REPORT, rv);
                putRequestScope(AttributeConst.TOKEN, getTokenId());
                putRequestScope(AttributeConst.ERR, errors);

                forward(ForwardConst.FW_REP_NEW);

            } else {
                //セッションに登録完了のメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());
                //リダイレクト
                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
            }
        }
    }

    //詳細画面の表示
    public void show() throws ServletException, IOException {
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        if (rv == null) {
            forward(ForwardConst.FW_ERR_UNKNOWN);
        } else {
            putRequestScope(AttributeConst.REPORT, rv);
            forward(ForwardConst.FW_REP_SHOW);
        }
    }

    //編集画面の表示
    public void edit() throws ServletException, IOException {
        //idを条件に日報データを取得
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //レポートが取得できないか、ログインしてる作業員が作成者ではない場合
        if (rv == null || ev.getId() != rv.getEmployee().getId()) {
            forward(ForwardConst.FW_ERR_UNKNOWN);
        } else {
            putRequestScope(AttributeConst.TOKEN, getTokenId());
            putRequestScope(AttributeConst.REPORT, rv);

            forward(ForwardConst.FW_REP_EDIT);
        }
    }

    public void update() throws ServletException, IOException {
        if (checkToken()) {
            ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

            rv.setReportDate(toLocalDate(getRequestParam(AttributeConst.REP_DATE)));
            rv.setTitle(getRequestParam(AttributeConst.REP_TITLE));
            rv.setContent(getRequestParam(AttributeConst.REP_CONTENT));

            //日報データの更新
            List<String> errors = service.update(rv);

            //エラー発生時
            if (errors.size() > 0) {
                putRequestScope(AttributeConst.TOKEN, getTokenId());
                putRequestScope(AttributeConst.REPORT, rv);
                putRequestScope(AttributeConst.ERR, errors);

                forward(ForwardConst.FW_REP_EDIT);
            } else {
                //エラーがなければ
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_UPDATED.getMessage());
                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
            }
        }
    }
}
