package it.bugbuster.asilapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;

import it.bugbuster.asilapp.entity.AsylumSeeker;
import it.bugbuster.asilapp.entity.User;
import it.bugbuster.asilapp.utils.UserAvatarUtil;

public class UserAdapter extends ArrayAdapter<User> {

    public UserAdapter(Context context, List<User> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_user, parent, false);
        }

        User user = getItem(position);

        TextView itemName = convertView.findViewById(R.id.item_name);
        TextView itemBirthdate = convertView.findViewById(R.id.item_birthdate);
        ImageView itemAvatar = convertView.findViewById(R.id.user_avatar);

        if (user != null) {
            String name = user.getName();
            String surname = user.getSurname();
            String nameSurname = name + " " + surname;
            itemName.setText(nameSurname);
            itemBirthdate.setText(user.getBirthDate());
            UserAvatarUtil.setUserAvatar(name, surname, itemAvatar);
        }

        return convertView;
    }
}
