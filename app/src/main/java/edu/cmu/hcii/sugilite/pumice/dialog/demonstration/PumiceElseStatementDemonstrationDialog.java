package edu.cmu.hcii.sugilite.pumice.dialog.demonstration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import edu.cmu.hcii.sugilite.SugiliteData;
import edu.cmu.hcii.sugilite.automation.ServiceStatusManager;
import edu.cmu.hcii.sugilite.dao.SugiliteScriptDao;
import edu.cmu.hcii.sugilite.dao.SugiliteScriptFileDao;
import edu.cmu.hcii.sugilite.model.block.SugiliteStartingBlock;
import edu.cmu.hcii.sugilite.pumice.PumiceDemonstrationUtil;
import edu.cmu.hcii.sugilite.pumice.dialog.intent_handler.else_statement.PumiceUserExplainElseStatementIntentHandler;
import edu.cmu.hcii.sugilite.pumice.ui.PumiceDialogActivity;
import edu.cmu.hcii.sugilite.verbal_instruction_demo.VerbalInstructionIconManager;

/**
 * @author toby
 * @date 12/17/18
 * @time 2:38 PM
 */

/**
 * dialog used for initiating a user demonstration for a procedure -- constructed and called from  the PumiceUserExplainProcedureIntentHandler
 */
public class PumiceElseStatementDemonstrationDialog {
    private AlertDialog dialog;
    private Activity context;
    private String boolExpReadableName;
    private String userUtterance;
    private PumiceUserExplainElseStatementIntentHandler parentIntentHandler;
    private SugiliteScriptDao sugiliteScriptDao;
    private SharedPreferences sharedPreferences;
    private SugiliteData sugiliteData;
    private ServiceStatusManager serviceStatusManager;
    private VerbalInstructionIconManager verbalInstructionIconManager;

    public PumiceElseStatementDemonstrationDialog(Activity context, String boolExpReadableName, String userUtterance, SharedPreferences sharedPreferences, SugiliteData sugiliteData, ServiceStatusManager serviceStatusManager, PumiceUserExplainElseStatementIntentHandler parentIntentHandler){
        this.context = context;
        this.boolExpReadableName = boolExpReadableName;
        this.userUtterance = userUtterance;
        this.parentIntentHandler = parentIntentHandler;
        this.sharedPreferences = sharedPreferences;
        this.sugiliteData = sugiliteData;
        this.verbalInstructionIconManager = sugiliteData.verbalInstructionIconManager;
        this.serviceStatusManager = serviceStatusManager;
        this.sugiliteScriptDao = new SugiliteScriptFileDao(context, sugiliteData);
        constructDialog();
    }

    private void constructDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage("Please start demonstrating what to do when " + boolExpReadableName +  " is not true. " + "Click OK to continue.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //String scriptName = "Procedure_" + procedureKnowledgeName; //determine the script name
                        String scriptName = "Procedure_" + "when " + boolExpReadableName + " is not true";

                        //create a callback to be called when the recording ends
                        Runnable onFinishDemonstrationCallback = new Runnable() {
                            @Override
                            public void run() {
                                SugiliteData.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //turn off the overlay
                                        if(verbalInstructionIconManager != null){
                                            verbalInstructionIconManager.turnOffCatOverlay();
                                        }

                                        //get the result script
                                        try {
                                            SugiliteStartingBlock script = sugiliteScriptDao.read(scriptName + ".SugiliteScript");
                                            if (script != null) {
                                                onDemonstrationReady(script);
                                            } else {
                                                throw new RuntimeException("can't find the script!");
                                            }
                                        } catch (Exception e){
                                            throw new RuntimeException("failed to read the script!");
                                        }
                                    }
                                });
                            }
                        };
                        PumiceDemonstrationUtil.initiateDemonstration(context, serviceStatusManager, sharedPreferences, scriptName, sugiliteData, onFinishDemonstrationCallback, sugiliteScriptDao, verbalInstructionIconManager);
                    }
                });
        dialog = dialogBuilder.create();
    }

    //called when the demonstration is ready
    public void onDemonstrationReady(SugiliteStartingBlock script){
        //resume the Sugilite agent activity
        Intent resumeActivity = new Intent(context, PumiceDialogActivity.class);
        resumeActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivityIfNeeded(resumeActivity, 0);

        PumiceDemonstrationUtil.showSugiliteToast("Demonstration Ready!", Toast.LENGTH_SHORT);


        //construct the procedure knowledge

        //run the returnResultCallback when the result if ready
        parentIntentHandler.returnUserExplainElseStatementResult(script);
    }

    //show the dialog
    public void show() {
        dialog.show();
    }
}