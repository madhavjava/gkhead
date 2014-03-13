package org.mifos.rest.approval.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name="rest_approval")
public class RESTApprovalEntity {

    public static final String FORMAT = "MMM dd, yyyy hh:mm:ss z";

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private ApprovalState state;

    @Lob
    private String methodContent;

    private Short createdBy;

    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime createdOn;

    private Short approvedBy;

    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime approvedOn;

    @Transient
    private ApprovalMethod approvalMethod;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethodContent() {
        return methodContent;
    }

    public void setMethodContent(String methodContent) {
        this.methodContent = methodContent;
    }

    public Short getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Short createdBy) {
        this.createdBy = createdBy;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Short getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Short approvedBy) {
        this.approvedBy = approvedBy;
    }

    public DateTime getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(DateTime approvedOn) {
        this.approvedOn = approvedOn;
    }

    public ApprovalState getState() {
        return state;
    }

    public void setState(ApprovalState state) {
        this.state = state;
    }

    @Transient
    public String getPrintableCreatedOnDate() {
        return (getApprovedOn() == null) ? null : getCreatedOn().toString(FORMAT);
    }

    @Transient
    public String getPrintableApprovedOnDate() {
        return (getApprovedOn() == null) ? null : getApprovedOn().toString(FORMAT);
    }

    @Transient
    public ApprovalMethod getApprovalMethod() throws Exception {
        if (approvalMethod == null) {
            ObjectMapper om = new ObjectMapper();
            approvalMethod = om.readValue(getMethodContent(), ApprovalMethod.class);
        }
        return approvalMethod;
    }

    @Transient
    public void setApprovalMethod(ApprovalMethod method) throws Exception {
        approvalMethod = method;
        ObjectMapper om = new ObjectMapper();
        String content = om.writeValueAsString(method);
        setMethodContent(content);
    }

    @Transient
    public String getType() throws Exception {
        return getApprovalMethod().getType().getSimpleName().replace("RESTController", "");
    }

    @Transient
    public String getOperation() throws Exception {
        return getApprovalMethod().getName();
    }

}
