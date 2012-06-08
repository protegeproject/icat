package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;

//RULE GROUP
public class RuleGroup implements Comparable<RuleGroup>, Serializable
 {
   public String groupName = "";
   public String signature = "";
   public int number;
   //private Boolean isEnabled = Boolean.FALSE;

   public RuleGroup() {}
   
   public RuleGroup(String groupName, String signature, int number) 
   {
     super();
     this.groupName = groupName;
     this.signature = signature;
     this.number = number;
     //this.isEnabled = isEnabled;
   }
   
   public String getGroupName() { return groupName; }
   public void setGroupName(String groupName) { this.groupName = groupName; }
   public String getSignature() { return signature; }
   public void setSignature(String signature) { this.groupName = signature; }
   public int getNumber() { return number; }
   public void setNumber(int number) { this.number = number; }
   //public Boolean getIsEnabled() { return isEnabled; }
   //public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
   
   public boolean equals(Object obj) {
     if (this == obj) return true;
     if (obj == null) return false;
     if (getClass() != obj.getClass()) return false;
     final RuleGroup other = (RuleGroup)obj;
     if (groupName == null) { if (other.groupName != null)return false;
     } else if (!groupName.equals(other.groupName)) return false;
     //if (isEnabled == null) { if (other.isEnabled != null) return false;
     //} else if (!isEnabled.equals(other.isEnabled)) return false;
     return true;
   }

   public int compareTo(RuleGroup otherObject) {
     int res = 0;
     res = otherObject.getGroupName().compareTo(getGroupName());
     //if (0 == res) { res = otherObject.getIsEnabled().compareTo(getIsEnabled()); }
     return res;
   } // compareTo

 } // RuleGroup