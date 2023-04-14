package com.edu.info7255.utils;

import java.util.HashMap;

public class UpgradedUnionSet {

    private final HashMap<Integer, Integer> managerMap = new HashMap<>();

    private final HashMap<Integer, Integer> parents = new HashMap<>();

    public void setPeer(int emp1, int emp2) {
        int emp1Parent = getParent(emp1);
        int emp2Parent = getParent(emp2);
        int manager1 = getManager(emp1);
        if (manager1 == -1) {
            parents.put(emp1Parent, emp2Parent);
        } else {
            parents.put(emp2Parent, emp1Parent);
        }
    }

    public void setManager(int emp, int manager) {
        managerMap.put(getParent(emp), manager);
    }

    public boolean isInManagementChain(int emp1, int emp2) {
        return isInManagementChainHelper(emp1, emp2) || isInManagementChainHelper(emp2, emp1);
    }

    private boolean isInManagementChainHelper(int emp1, int emp2) {
        int manager = getManager(emp1);
        while (manager != -1) {
            if (emp2 == manager) {
                return true;
            }
            manager = getManager(manager);
        }
        return false;
    }

    public int getParent(int person) {
        if (!parents.containsKey(person)) {
            parents.put(person, person);
        }
        return person == parents.get(person) ? person : getParent(parents.get(person));
    }

    public int getManager(int person) {
        return managerMap.getOrDefault(getParent(person), -1);
    }

}
