package com.example.findFrequency.tree;

import java.util.Map;

public class TreeLeaf implements TreePart {
    private End fin;

    public TreeLeaf(End fin) {
        this.fin = fin;
    }

    @Override
    public String compute(Map<String, Map<String, Double>> vals) {
        return fin.compute();
    }

    public interface End {
        String compute();
    }
}
