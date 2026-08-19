package state

import (
	"github.com/sweet-juice/sweetjuice/app"
	"github.com/sweet-juice/sweetjuice/plugins/calls"
	"github.com/sweet-juice/sweetjuice/plugins/sms"
)

// MainAppState holds the global application state.
type MainAppState struct {
	Waiting    bool
	WaitingMsg string
	CallLog    calls.CallLog
	SmsFolder  sms.SmsFolder
	CallLogMsg string
	SmsMsg     string
}

// NewMainAppState creates a new MainAppState.
func NewMainAppState() *MainAppState {
	return &MainAppState{}
}

// SetWaiting shows a waiting message.
func (s *MainAppState) SetWaiting(msg string) {
	s.Waiting = true
	s.WaitingMsg = msg
	app.ReRender()
}

// ClearWaiting hides the waiting message.
func (s *MainAppState) ClearWaiting() {
	s.Waiting = false
	s.WaitingMsg = ""
	app.ReRender()
}

// SetCallLogResult updates the call log result and re-renders.
func (s *MainAppState) SetCallLogResult(log calls.CallLog, msg string) {
	s.CallLog = log
	s.CallLogMsg = msg
	app.ReRender()
}

// SetSmsResult updates the SMS result and re-renders.
func (s *MainAppState) SetSmsResult(folder sms.SmsFolder, msg string) {
	s.SmsFolder = folder
	s.SmsMsg = msg
	app.ReRender()
}
