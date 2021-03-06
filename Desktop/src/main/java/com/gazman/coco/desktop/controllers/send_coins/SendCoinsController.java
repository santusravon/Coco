package com.gazman.coco.desktop.controllers.send_coins;

import com.gazman.coco.core.api.ErrorData;
import com.gazman.coco.core.api.SummeryData;
import com.gazman.coco.core.utils.StringUtils;
import com.gazman.coco.desktop.ScreensController;
import com.gazman.coco.desktop.miner.requests.CocoRequest;
import com.gazman.coco.desktop.miner.transactions.Transaction1To1Request;
import com.gazman.coco.desktop.popups.PopupBuilder;
import com.gazman.coco.desktop.settings.ClientSettings;
import com.gazman.lifecycle.Factory;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.bitcoinj.core.Base58;

/**
 * Created by Ilya Gazman on 6/23/2018.
 */
public class SendCoinsController {
    public TextField address;
    public TextField amountField;
    public CheckBox allCheckBox;
    private TransactionsModel transactionsModel = Factory.inject(TransactionsModel.class);

    private ScreensController screensController = Factory.inject(ScreensController.class);


    public void onContinue(MouseEvent mouseEvent) {
        if (transactionsModel.getTransactionDatas().size() == 1) {
            make1To1Request();
        } else {
            throw new Error("Not implemented");
        }
    }

    private void make1To1Request() {
        TransactionData transactionData = transactionsModel.getTransactionDatas().get(0);
        new Transaction1To1Request(ClientSettings.instance.defaultPoolData)
                .setAmount(transactionData.amount)
                .setReceiverId(Base58.decode(transactionData.recipient))
                .setPath("transaction")
                .setCallback(new CocoRequest.Callback<SummeryData>() {
                    @Override
                    public void onSuccess(SummeryData data) {
                        transactionsModel.summeryData = data;
                        Factory.inject(PopupBuilder.class)
                                .setMessage("You preview is ready")
                                .setTitle("Success")
                                .setPositiveButtonCallback(event -> screensController.sendCoinsPreviewScreen.open())
                                .execute();
                    }

                    @Override
                    public void onError(ErrorData errorData) {
                        Factory.inject(PopupBuilder.class)
                                .setMessage(errorData.code + "> " + errorData.error)
                                .setTitle("Error")
                                .execute();
                    }
                })
                .execute();
    }

    public void onAddTransaction(MouseEvent mouseEvent) {
        String reciepient = address.getText();
        if (StringUtils.isNullOrEmpty(reciepient)) {
            showError("Please add address");
            return;
        }

        if (StringUtils.isNullOrEmpty(amountField.getText())) {
            showError("Please specify amount");
            return;
        }
        double amount = Double.parseDouble(amountField.getText());

        if (transactionsModel.getTransactionDatas().size() > 0) {
            Factory.inject(PopupBuilder.class)
                    .setMessage("At the moment we only support single recipient, it will change soon ;)")
                    .setTitle("Not support yet")
                    .execute();
        }
        TransactionData transactionData = new TransactionData();
        transactionData.amount = amount;
        transactionData.recipient = reciepient;
        transactionsModel.addTransaction(transactionData);
    }

    private void showError(String errorMessage) {
        Factory.inject(PopupBuilder.class)
                .setMessage(errorMessage)
                .setTitle("Error")
                .execute();
    }
}
