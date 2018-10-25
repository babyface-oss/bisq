/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.components.paymentmethods;

import bisq.desktop.components.InputTextField;
import bisq.desktop.util.FormBuilder;
import bisq.desktop.util.Layout;
import bisq.desktop.util.validation.ClearXchangeValidator;

import bisq.core.locale.Res;
import bisq.core.locale.TradeCurrency;
import bisq.core.payment.AccountAgeWitnessService;
import bisq.core.payment.ClearXchangeAccount;
import bisq.core.payment.PaymentAccount;
import bisq.core.payment.payload.ClearXchangeAccountPayload;
import bisq.core.payment.payload.PaymentAccountPayload;
import bisq.core.util.BSFormatter;
import bisq.core.util.validation.InputValidator;

import org.apache.commons.lang3.StringUtils;

import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ClearXchangeForm extends PaymentMethodForm {
    private final ClearXchangeAccount clearXchangeAccount;
    private final ClearXchangeValidator clearXchangeValidator;
    private InputTextField mobileNrInputTextField;

    public static int addFormForBuyer(GridPane gridPane, int gridRow, PaymentAccountPayload paymentAccountPayload) {
        FormBuilder.addTopLabelTextFieldWithCopyIcon(gridPane, ++gridRow, Res.getWithCol("payment.account.owner"),
                ((ClearXchangeAccountPayload) paymentAccountPayload).getHolderName());
        FormBuilder.addTopLabelTextFieldWithCopyIcon(gridPane, ++gridRow, Res.get("payment.email.mobile"),
                ((ClearXchangeAccountPayload) paymentAccountPayload).getEmailOrMobileNr());
        return gridRow;
    }

    public ClearXchangeForm(PaymentAccount paymentAccount, AccountAgeWitnessService accountAgeWitnessService, ClearXchangeValidator clearXchangeValidator, InputValidator inputValidator, GridPane gridPane, int gridRow, BSFormatter formatter) {
        super(paymentAccount, accountAgeWitnessService, inputValidator, gridPane, gridRow, formatter);
        this.clearXchangeAccount = (ClearXchangeAccount) paymentAccount;
        this.clearXchangeValidator = clearXchangeValidator;
    }

    @Override
    public void addFormForAddAccount() {
        gridRowFrom = gridRow + 1;

        InputTextField holderNameInputTextField = FormBuilder.addInputTextField(gridPane, ++gridRow,
                Res.getWithCol("payment.account.owner"));
        holderNameInputTextField.setValidator(inputValidator);
        holderNameInputTextField.textProperty().addListener((ov, oldValue, newValue) -> {
            clearXchangeAccount.setHolderName(newValue);
            updateFromInputs();
        });

        mobileNrInputTextField = FormBuilder.addInputTextField(gridPane, ++gridRow,
                Res.get("payment.email.mobile"));
        mobileNrInputTextField.setValidator(clearXchangeValidator);
        mobileNrInputTextField.textProperty().addListener((ov, oldValue, newValue) -> {
            clearXchangeAccount.setEmailOrMobileNr(newValue);
            updateFromInputs();
        });
        final TradeCurrency singleTradeCurrency = clearXchangeAccount.getSingleTradeCurrency();
        final String nameAndCode = singleTradeCurrency != null ? singleTradeCurrency.getNameAndCode() : "";
        FormBuilder.addTopLabelTextField(gridPane, ++gridRow, Res.getWithCol("shared.currency"),
                nameAndCode);
        addLimitations();
        addAccountNameTextFieldWithAutoFillToggleButton();
    }

    @Override
    protected void autoFillNameTextField() {
        if (useCustomAccountNameToggleButton != null && !useCustomAccountNameToggleButton.isSelected()) {
            String mobileNr = mobileNrInputTextField.getText();
            mobileNr = StringUtils.abbreviate(mobileNr, 9);
            String method = Res.get(paymentAccount.getPaymentMethod().getId());
            accountNameTextField.setText(method.concat(": ").concat(mobileNr));
        }
    }

    @Override
    public void addFormForDisplayAccount() {
        gridRowFrom = gridRow;
        FormBuilder.addTopLabelTextField(gridPane, gridRow, Res.get("payment.account.name"),
                clearXchangeAccount.getAccountName(), Layout.FIRST_ROW_AND_GROUP_DISTANCE);
        FormBuilder.addTopLabelTextField(gridPane, ++gridRow, Res.getWithCol("shared.paymentMethod"),
                Res.get(clearXchangeAccount.getPaymentMethod().getId()));
        FormBuilder.addTopLabelTextField(gridPane, ++gridRow, Res.getWithCol("payment.account.owner"),
                clearXchangeAccount.getHolderName());
        TextField field = FormBuilder.addTopLabelTextField(gridPane, ++gridRow, Res.get("payment.email.mobile"),
                clearXchangeAccount.getEmailOrMobileNr()).second;
        field.setMouseTransparent(false);
        final TradeCurrency singleTradeCurrency = clearXchangeAccount.getSingleTradeCurrency();
        final String nameAndCode = singleTradeCurrency != null ? singleTradeCurrency.getNameAndCode() : "";
        FormBuilder.addTopLabelTextField(gridPane, ++gridRow, Res.getWithCol("shared.currency"),
                nameAndCode);
        addLimitations();
    }

    @Override
    public void updateAllInputsValid() {
        allInputsValid.set(isAccountNameValid()
                && clearXchangeValidator.validate(clearXchangeAccount.getEmailOrMobileNr()).isValid
                && inputValidator.validate(clearXchangeAccount.getHolderName()).isValid
                && clearXchangeAccount.getTradeCurrencies().size() > 0);
    }
}
