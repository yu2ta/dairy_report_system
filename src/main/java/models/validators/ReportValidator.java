package models.validators;

import java.util.ArrayList;
import java.util.List;

import actions.view.ReportView;
import constants.MessageConst;

public class ReportValidator {
    public static List<String> validate(ReportView rv) {

        List<String> errors = new ArrayList<String>();

        //タイトルのチェック
        String titleError = validateTitle(rv.getTitle());
        if (!titleError.equals("")) {
            errors.add(titleError);
        }

        //内容のチェック
        String contentError = validateContent(rv.getContent());
        if (!contentError.equals("")) {
            errors.add(contentError);
        }
        return errors;
    }

    //タイトルチェック
    public static String validateTitle(String title) {
        if (title == null || title.equals("")) {
            return MessageConst.E_NOTITLE.getMessage();
        }

        return "";
    }

    //コンテンツチェック
    public static String validateContent(String content) {
        if (content == null || content.equals("")) {
            return MessageConst.E_NOCONTENT.getMessage();
        }

        return "";
    }
}
