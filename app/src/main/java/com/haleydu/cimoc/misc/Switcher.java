package com.haleydu.cimoc.misc;

public class Switcher<T> {

    private T element;
    private boolean enable;

    public Switcher(T element, boolean enable) {
        this.element = element;
        this.enable = enable;
    }

    public T getElement() {
        return element;
    }

    public void setElement(T element) {
        this.element = element;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void switchEnable() {
        this.enable = !this.enable;
    }
}