package uk.gov.hmcts.reform.pcqconsolidationservice.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * This is the object representing the REST model for the PcqAnswers entity in the submit Answers API.
 * The PMD warnings are suppressed because this is a JSON object and hence can't be split into two different classes.
 * The properties below are not primitives because JSON interprets primitive as 0 if the value is not supplied which
 * has a different meaning to actual value coming from the front-end.
 */
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.TooManyFields", "PMD.UnnecessaryConstructor"})

public class PcqAnswers implements Serializable {

    public static final long serialVersionUID = 3328743L;

    @JsonProperty("dob_provided")
    private Integer dobProvided;

    @JsonProperty("dob")
    private String dob;

    @JsonProperty("language_main")
    private Integer languageMain;

    @JsonProperty("language_other")
    private String languageOther;

    @JsonProperty("english_language_level")
    private Integer englishLanguageLevel;

    @JsonProperty("sex")
    private Integer sex;

    @JsonProperty("gender_different")
    private Integer genderDifferent;

    @JsonProperty("gender_other")
    private String genderOther;

    @JsonProperty("sexuality")
    private Integer sexuality;

    @JsonProperty("sexuality_other")
    private String sexualityOther;

    @JsonProperty("marriage")
    private Integer marriage;

    @JsonProperty("ethnicity")
    private Integer ethnicity;

    @JsonProperty("ethnicity_other")
    private String ethnicityOther;

    @JsonProperty("religion")
    private Integer religion;

    @JsonProperty("religion_other")
    private String religionOther;

    @JsonProperty("disability_conditions")
    private Integer disabilityConditions;

    @JsonProperty("disability_impact")
    private Integer disabilityImpact;

    @JsonProperty("disability_vision")
    private Integer disabilityVision;

    @JsonProperty("disability_hearing")
    private Integer disabilityHearing;

    @JsonProperty("disability_mobility")
    private Integer disabilityMobility;

    @JsonProperty("disability_dexterity")
    private Integer disabilityDexterity;

    @JsonProperty("disability_learning")
    private Integer disabilityLearning;

    @JsonProperty("disability_memory")
    private Integer disabilityMemory;

    @JsonProperty("disability_mental_health")
    private Integer disabilityMentalHealth;

    @JsonProperty("disability_stamina")
    private Integer disabilityStamina;

    @JsonProperty("disability_social")
    private Integer disabilitySocial;

    @JsonProperty("disability_other")
    private Integer disabilityOther;

    @JsonProperty("disability_other_details")
    private String disabilityConditionOther;

    @JsonProperty("disability_none")
    private Integer disabilityNone;

    @JsonProperty("pregnancy")
    private Integer pregnancy;

    public PcqAnswers() {
        // Intentionally left blank.
    }

    public Integer getDobProvided() {
        return dobProvided;
    }

    public void setDobProvided(Integer dobProvided) {
        this.dobProvided = dobProvided;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public Integer getLanguageMain() {
        return languageMain;
    }

    public void setLanguageMain(Integer languageMain) {
        this.languageMain = languageMain;
    }

    public String getLanguageOther() {
        return languageOther;
    }

    public void setLanguageOther(String languageOther) {
        this.languageOther = languageOther;
    }

    public Integer getEnglishLanguageLevel() {
        return englishLanguageLevel;
    }

    public void setEnglishLanguageLevel(Integer englishLanguageLevel) {
        this.englishLanguageLevel = englishLanguageLevel;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getGenderDifferent() {
        return genderDifferent;
    }

    public void setGenderDifferent(Integer genderDifferent) {
        this.genderDifferent = genderDifferent;
    }

    public String getGenderOther() {
        return genderOther;
    }

    public void setGenderOther(String genderOther) {
        this.genderOther = genderOther;
    }

    public Integer getSexuality() {
        return sexuality;
    }

    public void setSexuality(Integer sexuality) {
        this.sexuality = sexuality;
    }

    public String getSexualityOther() {
        return sexualityOther;
    }

    public void setSexualityOther(String sexualityOther) {
        this.sexualityOther = sexualityOther;
    }

    public Integer getMarriage() {
        return marriage;
    }

    public void setMarriage(Integer marriage) {
        this.marriage = marriage;
    }

    public Integer getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(Integer ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getEthnicityOther() {
        return ethnicityOther;
    }

    public void setEthnicityOther(String ethnicityOther) {
        this.ethnicityOther = ethnicityOther;
    }

    public Integer getReligion() {
        return religion;
    }

    public void setReligion(Integer religion) {
        this.religion = religion;
    }

    public String getReligionOther() {
        return religionOther;
    }

    public void setReligionOther(String religionOther) {
        this.religionOther = religionOther;
    }

    public Integer getDisabilityConditions() {
        return disabilityConditions;
    }

    public void setDisabilityConditions(Integer disabilityConditions) {
        this.disabilityConditions = disabilityConditions;
    }

    public Integer getDisabilityImpact() {
        return disabilityImpact;
    }

    public void setDisabilityImpact(Integer disabilityImpact) {
        this.disabilityImpact = disabilityImpact;
    }

    public Integer getDisabilityVision() {
        return disabilityVision;
    }

    public void setDisabilityVision(Integer disabilityVision) {
        this.disabilityVision = disabilityVision;
    }

    public Integer getDisabilityHearing() {
        return disabilityHearing;
    }

    public void setDisabilityHearing(Integer disabilityHearing) {
        this.disabilityHearing = disabilityHearing;
    }

    public Integer getDisabilityMobility() {
        return disabilityMobility;
    }

    public void setDisabilityMobility(Integer disabilityMobility) {
        this.disabilityMobility = disabilityMobility;
    }

    public Integer getDisabilityDexterity() {
        return disabilityDexterity;
    }

    public void setDisabilityDexterity(Integer disabilityDexterity) {
        this.disabilityDexterity = disabilityDexterity;
    }

    public Integer getDisabilityLearning() {
        return disabilityLearning;
    }

    public void setDisabilityLearning(Integer disabilityLearning) {
        this.disabilityLearning = disabilityLearning;
    }

    public Integer getDisabilityMemory() {
        return disabilityMemory;
    }

    public void setDisabilityMemory(Integer disabilityMemory) {
        this.disabilityMemory = disabilityMemory;
    }

    public Integer getDisabilityMentalHealth() {
        return disabilityMentalHealth;
    }

    public void setDisabilityMentalHealth(Integer disabilityMentalHealth) {
        this.disabilityMentalHealth = disabilityMentalHealth;
    }

    public Integer getDisabilityStamina() {
        return disabilityStamina;
    }

    public void setDisabilityStamina(Integer disabilityStamina) {
        this.disabilityStamina = disabilityStamina;
    }

    public Integer getDisabilitySocial() {
        return disabilitySocial;
    }

    public void setDisabilitySocial(Integer disabilitySocial) {
        this.disabilitySocial = disabilitySocial;
    }

    public Integer getDisabilityOther() {
        return disabilityOther;
    }

    public void setDisabilityOther(Integer disabilityOther) {
        this.disabilityOther = disabilityOther;
    }

    public String getDisabilityConditionOther() {
        return disabilityConditionOther;
    }

    public void setDisabilityConditionOther(String disabilityConditionOther) {
        this.disabilityConditionOther = disabilityConditionOther;
    }

    public Integer getDisabilityNone() {
        return disabilityNone;
    }

    public void setDisabilityNone(Integer disabilityNone) {
        this.disabilityNone = disabilityNone;
    }

    public Integer getPregnancy() {
        return pregnancy;
    }

    public void setPregnancy(Integer pregnancy) {
        this.pregnancy = pregnancy;
    }
}
