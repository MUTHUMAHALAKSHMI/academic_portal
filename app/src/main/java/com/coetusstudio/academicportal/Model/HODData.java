package com.coetusstudio.academicportal.Model;

public class HODData {
    private String hodImage, hodName, hodEmail, hodId, hodBranch, hodPhone, hodPassword;

    public HODData() {
    }

    public HODData(String hodImage, String hodName, String hodEmail, String hodId, String hodBranch, String hodPhone, String hodPassword) {
        this.hodImage = hodImage;
        this.hodName = hodName;
        this.hodEmail = hodEmail;
        this.hodId = hodId;
        this.hodBranch = hodBranch;
        this.hodPhone = hodPhone;
        this.hodPassword = hodPassword;
    }

    public String getHodImage() {
        return hodImage;
    }

    public void setHodImage(String hodImage) {
        this.hodImage = hodImage;
    }

    public String getHodName() {
        return hodName;
    }

    public void setHodName(String hodName) {
        this.hodName = hodName;
    }

    public String getHodEmail() {
        return hodEmail;
    }

    public void setHodEmail(String hodEmail) {
        this.hodEmail = hodEmail;
    }

    public String getHodId() {
        return hodId;
    }

    public void setHodId(String hodId) {
        this.hodId = hodId;
    }

    public String getHodBranch() {
        return hodBranch;
    }

    public void setHodBranch(String hodBranch) {
        this.hodBranch = hodBranch;
    }

    public String getHodPhone() {
        return hodPhone;
    }

    public void setHodPhone(String hodPhone) {
        this.hodPhone = hodPhone;
    }

    public String getHodPassword() {
        return hodPassword;
    }

    public void setHodPassword(String hodPassword) {
        this.hodPassword = hodPassword;
    }
}
