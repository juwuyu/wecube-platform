package com.webank.wecube.platform.core.support.authserver;

import java.io.Serializable;

public class AsRoleDto implements Serializable{
    
    
    /**
     * 
     */
    private static final long serialVersionUID = -5419011196635473302L;
    private String id;
	private String name;
	private String displayName;
	private String email;
	
	private String status;//Deleted,NotDeleted
	
	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
	
	
}
