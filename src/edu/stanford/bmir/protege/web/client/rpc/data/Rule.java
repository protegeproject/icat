package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;


// RULE
public class Rule implements Comparable<Rule>,Serializable 
  {
    public RuleGroup group = null;
    public String ruleName = "";
    public String ruleText = "";
    //private Boolean isEnabled = Boolean.FALSE;
    
    public Rule() {}
    
    public Rule(RuleGroup group, String ruleName, String ruleText) 
    {
      super();
      this.group = group;
      this.ruleName = ruleName;
      this.ruleText = ruleText;
      
      //this.isEnabled = isEnabled;
    }
    
    public String getRuleName() { return ruleName; }
    public String getRuleGroupName() { return group.getGroupName(); }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public RuleGroup getRuleGroup() { return group; }
    public void setRuleGroup(RuleGroup group) { this.group = group; }
    public String getRuleText() { return ruleText; }
    public void setRuleText(String ruleText) { this.ruleText = ruleText; }
    //public Boolean getIsEnabled() { return isEnabled; }
    //public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    public String toString() { return ruleName + " " + ruleText + " " + getRuleGroupName(); }


    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final Rule other = (Rule)obj;
      if (group == null) { if (other.group != null) return false;
      } else if (!group.equals(other.group)) return false;
      if (ruleName == null) { if (other.ruleName != null) return false;
      } else if (!ruleName.equals(other.ruleName)) return false;
      if (ruleText == null) { if (other.ruleText != null) return false;
      } else if (!ruleText.equals(other.ruleText)) return false;
      //if (isEnabled == null) { if (other.isEnabled != null) return false;
      //} else if (!isEnabled.equals(other.isEnabled)) return false;
      return true;
    }

    public int compareTo(Rule otherObject) {
      int res = 0;
      res = otherObject.getRuleName().compareTo(getRuleName());
      if (0 == res) { res = otherObject.getRuleGroup().compareTo(getRuleGroup()); }
      if (0 == res) { res = otherObject.getRuleText().compareTo(getRuleText()); }
      //if (0 == res) { res = otherObject.getIsEnabled().compareTo(getIsEnabled()); }
      return res;
    }
  } // Rule
