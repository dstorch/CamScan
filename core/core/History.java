package core;

import java.util.*;

public class History {

	private List<Event> _history;
	private int _currentIndex;
	private Event _currentEvent;
	
	public History() {
		_history = new ArrayList<Event>();
		_currentIndex = -1;
		_currentEvent = null;
	}
	
	/**
	 * Adds the event to the history list,
	 * and updates the current event and
	 * current index appropriately.
	 * 
	 * @param e - the event object to add
	 */
	public void addEvent(Event e) {
		
		// delete all following events if the current
		// event is not at the end of the list
		if (_currentIndex < (_history.size() - 1)) {
			for (int i = (_currentIndex + 1); i < _history.size(); i++) {
				_history.remove(i);
			}
		}
		
		// then add to the end of the list
		_history.add(e);
		_currentEvent = e;
		_currentIndex = _history.size() - 1;
	}
	
	public Event back() {
		if (_currentIndex > 0) {
			_currentIndex--;
			_currentEvent = _history.get(_currentIndex);
			return _currentEvent;
		} else {
			return null;
		}
	}
	
	public Event next() {
		if (_currentIndex >= _history.size()) {
			return null;
		} else {
			_currentIndex++;
			_currentEvent = _history.get(_currentIndex);
			return _currentEvent;
		}
	}
	
}
