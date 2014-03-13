package org.mifos.test.acceptance.framework.questionnaire;

import java.util.ArrayList;
import java.util.List;

public class CreateQuestionParameters {

    public static final String TYPE_FREE_TEXT = "Free Text";
    public static final String TYPE_DATE = "Date";
    public static final String TYPE_NUMBER = "Number";
    public static final String TYPE_MULTI_SELECT = "Multi Select";
    public static final String TYPE_SINGLE_SELECT = "Single Select";
    public static final String TYPE_SMART_SELECT = "Smart Select";

    private String text;

    private String type;
    private List<Choice> choices;
    private Integer numericMin;
    private Integer numericMax;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getChoicesAsStrings() {
        List<String> chStrs = null;
        if (choices != null) {
            chStrs = new ArrayList<String>(choices.size());
            for (Choice choice : choices) {
                chStrs.add(choice.getChoiceText());
            }
        }
        return chStrs;
    }

    public void setChoicesFromStrings(List<String> choices) {
        if (choices != null) {
            this.choices = new ArrayList<Choice>(choices.size());
            for (String choice : choices) {
                this.choices.add(getChoice(choice));
            }
        }
    }

    private Choice getChoice(String choice) {
        return new Choice(choice, null);
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public void setNumericMin(Integer numericMin) {
        this.numericMin = numericMin;
    }

    public void setNumericMax(Integer numericMax) {
        this.numericMax = numericMax;
    }

    public Integer getNumericMin() {
        return numericMin;
    }

    public Integer getNumericMax() {
        return numericMax;
    }

    boolean isNumericQuestionType() {
        return "Number".equals(getType());
    }

    public boolean questionHasAnswerChoices() {
        return "Multi Select".equals(getType()) || "Single Select".equals(getType());
    }

    public boolean questionHasSmartAnswerChoices() {
        return "Smart Select".equals(getType());
    }
}
