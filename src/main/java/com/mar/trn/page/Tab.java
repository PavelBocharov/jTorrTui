package com.mar.trn.page;

public record Tab(String title, Page page, int order) implements Comparable<Tab> {

    @Override
    public int compareTo(Tab o) {
        int order = Integer.compare(order(), o.order());
        if (order == 0) {
            return title().compareToIgnoreCase(o.title());
        }
        return order;
    }

}
