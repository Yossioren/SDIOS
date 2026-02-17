package com.example.findFrequency.tree;

import java.util.Map;

public class TreeNode implements TreePart {
    private TreePart left;
    private GoRightIfTrue fun;
    private TreePart right;
    private String st;

    public TreeNode(TreePart left, GoRightIfTrue fun, TreePart right, String st) {
        this.left = left;
        this.fun = fun;
        this.right = right;
        this.st = st;
    }

    @Override
    public String compute(Map<String, Map<String, Double>> vals) {
        String out = st;
        if (fun.compute(vals)) out += "-right->" + right.compute(vals);
        else out += "-left->" + left.compute(vals);
        return out;
    }

    public interface GoRightIfTrue {
        boolean compute(Map<String, Map<String, Double>> vals);
    }
}
