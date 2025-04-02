package it.bugbuster.asilapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DocumentAndContactDialog {
    public static void showAsylumInfoDialog(Context context) {
        String messageText = context.getString(R.string.asylum_info_message);
        SpannableString message = new SpannableString(messageText);

        ClickableSpan identityDocumentLink = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.cartaidentita.interno.gov.it/richiedi/"));
                widget.getContext().startActivity(browserIntent);
            }
        };
        ClickableSpan asylumFormLink = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://portaleimmigrazione.eu/wp-content/uploads/2018/03/Modello_C3.pdf"));
                widget.getContext().startActivity(browserIntent);
            }
        };
        ClickableSpan residenceLink = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://questure.poliziadistato.it/statics/50/all.-4---rilascio-permesso-di-soggiorno-provvisorio.pdf"));
                widget.getContext().startActivity(browserIntent);
            }
        };
        ClickableSpan commissionLink = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.libertaciviliimmigrazione.dlci.interno.gov.it/it/commissione-nazionale-diritto-asilo"));
                widget.getContext().startActivity(browserIntent);
            }
        };
        ClickableSpan questuraLink = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://questure.poliziadistato.it/stranieri/"));
                widget.getContext().startActivity(browserIntent);
            }
        };
        ClickableSpan supportLink = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.unhcr.org/it/cosa-facciamo/partner/progetti/arci/"));
                widget.getContext().startActivity(browserIntent);
            }
        };

        message.setSpan(identityDocumentLink, messageText.indexOf(context.getString(R.string.identity_document)),
                messageText.indexOf(context.getString(R.string.identity_document)) + context.getString(R.string.identity_document).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        message.setSpan(asylumFormLink, messageText.indexOf(context.getString(R.string.asylum_form)),
                messageText.indexOf(context.getString(R.string.asylum_form)) + context.getString(R.string.asylum_form).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        message.setSpan(residenceLink, messageText.indexOf(context.getString(R.string.temporary_residence)),
                messageText.indexOf(context.getString(R.string.temporary_residence)) + context.getString(R.string.temporary_residence).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        message.setSpan(commissionLink, messageText.indexOf(context.getString(R.string.commission_territorial)),
                messageText.indexOf(context.getString(R.string.commission_territorial)) + context.getString(R.string.commission_territorial).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        message.setSpan(questuraLink, messageText.indexOf(context.getString(R.string.immigration_office)),
                messageText.indexOf(context.getString(R.string.immigration_office)) + context.getString(R.string.immigration_office).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        message.setSpan(supportLink, messageText.indexOf(context.getString(R.string.support_associations)),
                messageText.indexOf(context.getString(R.string.support_associations)) + context.getString(R.string.support_associations).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.asylum_info_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();

        dialog.show();
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
