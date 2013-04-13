"""
Events related to the Motion process.
"""
from dropbox import client, session
from os.path import basename
from watchdog.events import FileSystemEventHandler

import os
import webbrowser


class SnapshotEventHandler(FileSystemEventHandler):
    """
    An event handler for acting on snapshot file creations.
    """

    def __init__(self, motion_process):
        super(SnapshotEventHandler, self).__init__()
        self.motion_process = motion_process

        if self.has_dropbox_configuration(motion_process):
            self.authenticate_dropbox(motion_process.settings)

    def has_dropbox_configuration(self, motion_process):
        """
        Check whether the Dropbox configuration is complete. Otherwise the
        Dropbox authentication will be skipped.
        """

        settings = motion_process.settings
        return (settings['dropbox_api_key'] and
                settings['dropbox_app_secret'] and
                settings['dropbox_access_type'])

    def authenticate_dropbox(self, settings):
        """
        Authenticates the application for the user's Dropbox account.
        """

        db_session = session.DropboxSession(settings['dropbox_api_key'],
                                            settings['dropbox_app_secret'],
                                            settings['dropbox_access_type'])

        request_token = db_session.obtain_request_token()
        url = db_session.build_authorize_url(request_token)

        save_output = os.dup(1)
        os.close(1)
        os.open(os.devnull, os.O_RDWR)
        try:
            webbrowser.open(url)
        finally:
            os.dup2(save_output, 1)

        print 'Please visit:', url
        raw_input("After authorization press 'Enter' to continue")

        db_session.obtain_access_token(request_token)
        self.dropbox_client = client.DropboxClient(db_session)

    def backup_snapshot(self, path, filename):
        """
        Runs an asynchronous routine for uploading the snapshot to Dropbox.
        """

        if self.dropbox_client:
            f = open(path)
            self.dropbox_client.put_file(filename, f)
            f.close()

    def on_modified(self, event):
        """
        Triggered when a snapshot is created. This event is used to notify the
        client about the the final creation of snapshot without using busy
        waiting.
        """

        path = event.src_path
        filename = basename(path)
        self.backup_snapshot(path, filename)

        if filename.endswith('snapshot.jpg'):
            self.motion_process.notify_about_snapshot(filename)
        elif filename == 'lastsnap.jpg':
            pass  # swallow
        else:
            self.motion_process.alert(filename)
