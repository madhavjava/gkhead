package org.mifos.platform.questionnaire.domain;

import java.io.Serializable;
import java.util.Set;

public class QuestionChoiceEntity implements Serializable {
    private static final long serialVersionUID = -257980669112925272L;

    private Integer choiceId;
    private String choiceText;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SE_BAD_FIELD", justification="Can't map to serializable sets from hibernate. e.g. HashSet - sad but true!")
    private Set<ChoiceTagEntity> tags;
    private Integer choiceOrder;

    // TODO: Can be protected? copy-paste from org.mifos.customers.surveys.business.QuestionChoice
    // defining the null constructor avoids some harmless hibernate error
    // messages during testing
    @SuppressWarnings("PMD.UncommentedEmptyConstructor")
    public QuestionChoiceEntity() {
    }

    public QuestionChoiceEntity(String text) {
        choiceText = text;
    }

    public Integer getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(Integer choiceId) {
        this.choiceId = choiceId;
    }

    public String getChoiceText() {
        return choiceText;
    }

    public void setChoiceText(String choiceText) {
        this.choiceText = choiceText;
    }

    public Set<ChoiceTagEntity> getTags() {
        return tags;
    }

    public void setTags(Set<ChoiceTagEntity> tags) {
        this.tags = tags;
    }

    public Integer getChoiceOrder() {
        return choiceOrder;
    }

    public void setChoiceOrder(Integer choiceOrder) {
        this.choiceOrder = choiceOrder;
    }
}
