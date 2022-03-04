package com.ptit.messenger.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ptit.messenger.R;
import com.ptit.messenger.activities.adapters.MessageApdapter;
import com.ptit.messenger.activities.model.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private String name_reciver;
    private String uid_reciver;
    MessageApdapter messageApdapter;
    ArrayList<Message> messages;
    FirebaseDatabase db;
    FirebaseStorage storage;
    private ImageView sendMessage, image_attachment;
    private EditText txtMessage;
    private RecyclerView recyclerView;

    private String senderRoom, receiverRoom, currentId, receiverAvatar, senderAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        this.name_reciver = getIntent().getStringExtra("name");
        this.uid_reciver = getIntent().getStringExtra("uid");
        sendMessage = findViewById(R.id.sendmessage);
        txtMessage = findViewById(R.id.messagebox);
        image_attachment = findViewById(R.id.image_attachment);
        getSupportActionBar().setTitle(name_reciver);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.currentId = FirebaseAuth.getInstance().getUid();
        this.senderRoom = currentId + uid_reciver;
        this.receiverRoom = uid_reciver + currentId;
        this.receiverAvatar = getIntent().getStringExtra("avatar");
        recyclerView = findViewById(R.id.recyclerviewChat);
        db = FirebaseDatabase.getInstance("https://messenger-9715a-default-rtdb.asia-southeast1.firebasedatabase.app");
        storage = FirebaseStorage.getInstance();
        messages = new ArrayList<>();
        messageApdapter = new MessageApdapter(this, messages, receiverAvatar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageApdapter);


        image_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });


        db.getReference().child("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                senderAvatar = dataSnapshot.child("profileImage").getValue(String.class);
            }
        });


        db.getReference().child("Chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            messages.add(message);
                        }
                        messageApdapter.notifyDataSetChanged();
                        if (messages.size() > 0) {
                            recyclerView.smoothScrollToPosition(messages.size() - 1);
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = txtMessage.getText().toString();
                Date date = new Date();
                Message message = new Message(messageText, currentId, date.getTime());
                txtMessage.setText("");

                String randomeKey = db.getReference().push().getKey();
                if (!messageText.trim().isEmpty()) {
                    db.getReference().child("Chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(randomeKey)
                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            db.getReference().child("Chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(randomeKey)
                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    HashMap<String, Object> lastObj = new HashMap<>();
                                    lastObj.put("lastMsg", message.getMessage());
                                    lastObj.put("lastMsgTime", date.getTime());

                                    db.getReference().child("Chats").child(senderRoom).updateChildren(lastObj);
                                    db.getReference().child("Chats").child(receiverRoom).updateChildren(lastObj);
                                }
                            });
                        }
                    });
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 25) {
            if (data != null) {
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("Chats").child(calendar.getTimeInMillis() + "");
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        String messageText = txtMessage.getText().toString();
                                        Date date = new Date();
                                        Message message = new Message(messageText, currentId, date.getTime());
                                        message.setImageUrl(imageUrl);
                                        message.setMessage("send the photo...");
                                        txtMessage.setText("");

                                        String randomeKey = db.getReference().push().getKey();
                                        db.getReference().child("Chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomeKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                db.getReference().child("Chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomeKey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        HashMap<String, Object> lastObj = new HashMap<>();
                                                        lastObj.put("lastMsg", message.getMessage());
                                                        lastObj.put("lastMsgTime", date.getTime());

                                                        db.getReference().child("Chats").child(senderRoom).updateChildren(lastObj);
                                                        db.getReference().child("Chats").child(receiverRoom).updateChildren(lastObj);
                                                    }
                                                });
                                            }
                                        });

                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();

    }


}