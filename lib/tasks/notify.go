package tasks

import (
	"github.com/sweet-juice/sweetjuice/plugins/notification"
)

// notify posts a system notification with the given title and body.
func notify(title, body string) {
	_, _ = notification.NewPlugin().Post(notification.Notification{
		Title:       title,
		Body:        body,
		ChannelID:   "default_channel",
		ChannelName: "General Notifications",
		Importance:  notification.ImportanceDefault,
	})
}
