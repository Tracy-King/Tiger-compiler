package Semant;

import Translate.Access;

abstract class Entry{
	private Access access;
	
	public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
}
