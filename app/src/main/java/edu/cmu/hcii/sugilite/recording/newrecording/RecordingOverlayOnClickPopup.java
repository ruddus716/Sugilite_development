package edu.cmu.hcii.sugilite.recording.newrecording;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.hcii.sugilite.Const;
import edu.cmu.hcii.sugilite.model.Node;
import edu.cmu.hcii.sugilite.R;
import edu.cmu.hcii.sugilite.SugiliteData;
import edu.cmu.hcii.sugilite.model.block.SugiliteOperationBlock;
import edu.cmu.hcii.sugilite.model.operation.SugiliteOperation;
import edu.cmu.hcii.sugilite.model.operation.unary.SugiliteClickOperation;
import edu.cmu.hcii.sugilite.ontology.*;
import edu.cmu.hcii.sugilite.recording.ReadableDescriptionGenerator;
import edu.cmu.hcii.sugilite.verbal_instruction_demo.VerbalInstructionOverlayManager;
import edu.cmu.hcii.sugilite.verbal_instruction_demo.VerbalInstructionRecordingManager;

import static edu.cmu.hcii.sugilite.Const.OVERLAY_TYPE;

/**
 * @author toby
 * @date 2/5/18
 * @time 2:24 PM
 */
@Deprecated
public class RecordingOverlayOnClickPopup {
    //this dialog should allow the user to choose the operation after the user clicking on an overlay
    private Context context;
    private AlertDialog dialog;
    private VerbalInstructionOverlayManager overlayManager;
    private VerbalInstructionRecordingManager verbalInstructionRecordingManager;
    private SugiliteData sugiliteData;
    private SharedPreferences sharedPreferences;
    private ReadableDescriptionGenerator readableDescriptionGenerator;


    public RecordingOverlayOnClickPopup(Context context, Node node, SugiliteData sugiliteData, SharedPreferences sharedPreferences){
        this.context = context;
        this.overlayManager = overlayManager;
        this.sugiliteData = sugiliteData;
        this.sharedPreferences = sharedPreferences;
        this.verbalInstructionRecordingManager = new VerbalInstructionRecordingManager(context, sugiliteData, sharedPreferences);
        this.readableDescriptionGenerator = new ReadableDescriptionGenerator(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Const.appNameUpperCase + " Overlay Operation");

        List<String> operationList = new ArrayList<>();

        //fill in the options
        operationList.add("Click");
        operationList.add("Cancel");
        String[] operations = new String[operationList.size()];
        operations = operationList.toArray(operations);
        final String[] operationClone = operations.clone();


        builder.setItems(operationClone, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (operationClone[which]){
                    case "Click":
                        //generate a sugilite operation block that clicks on the object
                        addSugiliteOperationBlock(node, sugiliteData);
                        break;
                    case "Cancel":

                        break;
                }
            }
        });
        dialog = builder.create();
    }

    public void show(){
        dialog.getWindow().setType(OVERLAY_TYPE);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_box);
        dialog.show();
    }

    private void addSugiliteOperationBlock(Node node, SugiliteData sugiliteData){
        if(node.getBoundsInScreen() != null) {
            CombinedOntologyQuery parentQuery = new CombinedOntologyQuery(CombinedOntologyQuery.RelationType.AND);
            LeafOntologyQuery screenBoundsQuery = new LeafOntologyQuery();
            screenBoundsQuery.addObject(new SugiliteEntity<>(-1, String.class, node.getBoundsInScreen()));
            screenBoundsQuery.setQueryFunction(SugiliteRelation.HAS_SCREEN_LOCATION);
            parentQuery.addSubQuery(screenBoundsQuery);

            if (node.getClassName() != null) {
                LeafOntologyQuery classQuery = new LeafOntologyQuery();
                classQuery.addObject(new SugiliteEntity<>(-1, String.class, node.getClassName()));
                classQuery.setQueryFunction(SugiliteRelation.HAS_CLASS_NAME);
                parentQuery.addSubQuery(classQuery);
            }

            if (node.getPackageName() != null) {
                LeafOntologyQuery packageQuery = new LeafOntologyQuery();
                packageQuery.addObject(new SugiliteEntity<>(-1, String.class, node.getPackageName()));
                packageQuery.setQueryFunction(SugiliteRelation.HAS_PACKAGE_NAME);
                parentQuery.addSubQuery(packageQuery);
            }
            SugiliteOperationBlock operationBlock = generateBlock(parentQuery, parentQuery.toString());


            //add the operation block to the instruction queue specified in sugiliteData
            sugiliteData.addInstruction(operationBlock);
        }


    }

    private SugiliteOperationBlock generateBlock(OntologyQuery query, String formula){
        //generate the sugilite operation
        SugiliteClickOperation sugiliteOperation = new SugiliteClickOperation();
        //assume it's click for now -- need to expand to more types of operations
        sugiliteOperation.setOperationType(SugiliteOperation.CLICK);

        SugiliteOperationBlock operationBlock = new SugiliteOperationBlock();
        operationBlock.setOperation(sugiliteOperation);
        operationBlock.setFeaturePack(null);
        operationBlock.setElementMatchingFilter(null);
        operationBlock.setScreenshot(null);
        sugiliteOperation.setQuery(query.clone());

        operationBlock.setDescription(readableDescriptionGenerator.generateDescriptionForVerbalBlock(operationBlock, formula, "UTTERANCE"));
        return operationBlock;
    }
}
