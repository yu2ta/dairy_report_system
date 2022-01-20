package models.validators;

import java.util.ArrayList;
import java.util.List;

import actions.view.EmployeeView;
import constants.MessageConst;
import services.EmployeeService;

/**
 * 従業員インスタンスに設定されている値のバリデーションを行うクラス
 */

public class EmployeeValidator {
    /**
     * 従業員インスタンスの各項目についてバリデーションを行う
     * @param service 呼び出し元Serviceクラスのインスタンス
     * @param ev EmployeeServiceのインスタンス
     * @param codeDuplicateCheckFlag 社員番号の重複チェックを実施するかどうか(実施する:true 実施しない:false)
     * @param passwordCheckFlag パスワードの入力チェックを実施するかどうか(実施する:true 実施しない:false)
     * @return エラーのリスト
     */

    public static List<String> validate(EmployeeService service, EmployeeView ev, Boolean codeDuplicateCheckFlag,
            Boolean passwordCheckFlag) {
        List<String> errors = new ArrayList<String>();

        //社員番号チェック
        String codeError = validateCode(service, ev.getCode(), codeDuplicateCheckFlag);
        if (!codeError.equals("")) {
            errors.add(codeError);
        }

        //氏名チェック
        String nameError = validateName(ev.getName());
        if (!nameError.equals("")) {
            errors.add(nameError);
        }

        //パスワードのチェック
        String passError = validatePassword(ev.getPassword(), passwordCheckFlag);
        if (!passError.equals("")) {
            errors.add(passError);
        }

        return errors;
    }

    private static String validateCode(EmployeeService service, String code, Boolean codeDuplicateChBoolean) {
        if (code == null || code.equals("")) {
            return MessageConst.E_NOEMP_CODE.getMessage();
        }

        if (codeDuplicateChBoolean) {
            long employeeCount = isDuplicateEmployee(service, code);

            if (employeeCount > 0) {
                return MessageConst.E_EMP_CODE_EXIST.getMessage();
            }
        }
        return "";
    }

    private static long isDuplicateEmployee(EmployeeService service, String code) {
        long employeeCount = service.countByCode(code);
        return employeeCount;
    }

    private static String validateName(String name) {
        if (name == null || name.equals("")) {
            return MessageConst.E_NONAME.getMessage();
        }
        return "";
    }

    private static String validatePassword(String password, Boolean passwordCheckFlag) {
        if (passwordCheckFlag && (password == null || password.equals(""))) {
            return MessageConst.E_NOPASSWORD.getMessage();
        }

        return "";
    }
}
