package uk.gov.hmcts.reform.pcqconsolidationservice.controllers.response;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pcqconsolidationservice.controller.response.PcqAnswers;

import static org.junit.Assert.assertEquals;

class PcqAnswersTest {

    @Test
    void testPcqAnswers() {
        PcqAnswers answers = new PcqAnswers();
        addTestAnswers(answers);
        assertAnswers(answers);
    }

    private void addTestAnswers(PcqAnswers answers) {
        answers.setDisabilityConditionOther("TEST_DIS_OTHER");
        answers.setDisabilityConditions(1);
        answers.setDisabilityDexterity(0);
        answers.setDisabilityHearing(2);
        answers.setDisabilityImpact(1);
        answers.setDisabilityLearning(2);
        answers.setDisabilityMemory(0);
        answers.setDisabilityMentalHealth(3);
        answers.setDisabilityMobility(1);
        answers.setDisabilityNone(2);
        answers.setDisabilityOther(0);
        answers.setDisabilitySocial(2);
        answers.setDisabilityStamina(1);
        answers.setDisabilityVision(0);
        answers.setDob("01-01-1900");
        answers.setDobProvided(1);
        answers.setEnglishLanguageLevel(1);
        answers.setEthnicity(2);
        answers.setEthnicityOther("OK");
        answers.setGenderDifferent(2);
        answers.setGenderOther("OtherG");
        answers.setLanguageMain(1);
        answers.setLanguageOther("Lang1");
        answers.setMarriage(1);
        answers.setPregnancy(2);
        answers.setReligion(1);
        answers.setReligionOther("CDS");
        answers.setSex(2);
        answers.setSexuality(4);
        answers.setSexualityOther("Other");
    }

    private void assertAnswers(PcqAnswers answers) {
        assertEquals("Other Disability Condition is not matching", "TEST_DIS_OTHER",
                answers.getDisabilityConditionOther());
        assertEquals("Disability Conditions is not matching", 1,
                answers.getDisabilityConditions().intValue());
        assertEquals("Disability Dexterity is not matching", 0,
                answers.getDisabilityDexterity().intValue());
        assertEquals("Disability Hearing is not matching", 2,
                answers.getDisabilityHearing().intValue());
        assertEquals("Disability Impact is not matching", 1,
                answers.getDisabilityImpact().intValue());
        assertEquals("Disability Learning is not matching", 2,
                answers.getDisabilityLearning().intValue());
        assertEquals("Disability Memory is not matching", 0,
                answers.getDisabilityMemory().intValue());
        assertEquals("Disability Mental Health is not matching", 3,
                answers.getDisabilityMentalHealth().intValue());
        assertEquals("Disability Mobility is not matching", 1,
                answers.getDisabilityMobility().intValue());
        assertEquals("Disability None is not matching", 2,
                answers.getDisabilityNone().intValue());
        assertEquals("Disability Other is not matching", 0,
                answers.getDisabilityOther().intValue());
        assertEquals("Disability Social is not matching", 2,
                answers.getDisabilitySocial().intValue());
        assertEquals("Disability Stamina is not matching", 1,
                answers.getDisabilityStamina().intValue());
        assertEquals("Disability Vision is not matching", 0,
                answers.getDisabilityVision().intValue());
        assertEquals("Dob is not matching", "01-01-1900",
                answers.getDob());
        assertEquals("Dob Provided is not matching", 1,
                answers.getDobProvided().intValue());
        assertEquals("English Language level is not matching", 1,
                answers.getEnglishLanguageLevel().intValue());
        assertEquals("Ethnicity is not matching", 2,
                answers.getEthnicity().intValue());
        assertEquals("Ethnicity Other is not matching", "OK",
                answers.getEthnicityOther());
        assertEquals("Gender Different is not matching", 2,
                answers.getGenderDifferent().intValue());
        assertEquals("Gender Other is not matching", "OtherG",
                answers.getGenderOther());
        assertEquals("Main Language is not matching", 1,
                answers.getLanguageMain().intValue());
        assertEquals("Language other is not matching", "Lang1",
                answers.getLanguageOther());
        assertEquals("Marriage is not matching", 1,
                answers.getMarriage().intValue());
        assertEquals("Pregnancy is not matching", 2,
                answers.getPregnancy().intValue());
        assertEquals("Religion is not matching", 1,
                answers.getReligion().intValue());
        assertEquals("Religion Other is not matching", "CDS",
                answers.getReligionOther());
        assertEquals("Sex is not matching", 2,
                answers.getSex().intValue());
        assertEquals("Sexuality is not matching", 4,
                answers.getSexuality().intValue());
        assertEquals("Sexuality Other is not matching", "Other",
                answers.getSexualityOther());
    }
}
