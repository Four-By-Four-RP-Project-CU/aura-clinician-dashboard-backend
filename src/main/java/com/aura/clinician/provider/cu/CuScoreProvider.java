package com.aura.clinician.provider.cu;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.aura.clinician.api.dto.ScoreItem;
import com.aura.clinician.provider.ScoreProvider;

@Component
public class CuScoreProvider implements ScoreProvider {
    @Override
    public List<ScoreItem> getScores(String caseId) {
        List<ScoreItem> scores = new ArrayList<>();

        ScoreItem uct = new ScoreItem();
        uct.setCode("UCT");
        uct.setLabel("Urticaria Control Test");
        uct.setValue(10.0);
        uct.setInterpretation("Partially controlled (target >= 12)");
        uct.setWarning(true);
        scores.add(uct);

        ScoreItem aect = new ScoreItem();
        aect.setCode("AECT");
        aect.setLabel("Angioedema Control Test");
        aect.setValue(8.0);
        aect.setInterpretation("Moderate impact on daily life");
        aect.setWarning(true);
        scores.add(aect);

        return scores;
    }
}
