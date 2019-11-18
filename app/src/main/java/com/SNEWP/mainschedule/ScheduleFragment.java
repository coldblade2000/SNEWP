package com.SNEWP.mainschedule;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Time;
import com.github.tlaabs.timetableview.TimetableView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScheduleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment {
    //https://github.com/tlaabs/TimetableView


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ID_LIST = "zonasID";
    private static final String TAG = "ScheduleFrag";

    private ArrayList<String> mParam1;
    private ArrayList<Apunte> apuntes;

    private TimetableView timetable;

    private OnFragmentInteractionListener mListener;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ScheduleFragment.
     */
    public static ScheduleFragment newInstance(ArrayList<String> param1) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_ID_LIST, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        timetable = view.findViewById(R.id.timetable);

        apuntes=new ArrayList<>();
        if (getArguments() != null) {
            mParam1 = getArguments().getStringArrayList(ARG_ID_LIST);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("zonas").get()
                    .addOnSuccessListener(command ->{
                        for(DocumentSnapshot doc : command.getDocuments()){
                            Map<String, Object> map = doc.getData();
                            if(mParam1.contains(doc.getId())){
                                //En cada zona que esta seleccionada, consigue el contenido de la coleccion apuntes y la itera
                                db.collection("zonas").document(doc.getId()).collection("apuntes").get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                String accountID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                for (QueryDocumentSnapshot doc1 : task.getResult()) { //Cada apunte de una zona
                                                    ArrayList<Schedule> schedules = new ArrayList<>();
                                                    Apunte apunte = new Apunte(doc1.getData(), accountID);
                                                    apunte.setApunteID(doc1.getId());
                                                    apuntes.add(apunte);

//                                                    schedules.addAll(getSchedules(apuntes));
                                                    for(Schedule s: getSchedules(apunte)){
                                                        boolean isSInSchedules = false;
                                                        for(Schedule k : schedules){
                                                            if(k.getId().equals(s.getId()) && k.getDay() == s.getDay()){
                                                                isSInSchedules = true;
                                                                break;
                                                            }
                                                        }
                                                        if(!isSInSchedules){
                                                            schedules.add(s);
                                                        }
                                                    }

                                                    timetable.add(schedules);

                                                }
                                            } else {
                                                Log.e(TAG, "Error getting documents: ", task.getException());
                                            }
                                        });

                            }
                        }
                    });


        }

//        timetable.add(debugSchedule());
//        timetable.add(getSchedules(apuntes));

        timetable.setOnStickerSelectEventListener(new TimetableView.OnStickerSelectedListener() {
            @Override
            public void OnStickerSelected(int idx, ArrayList<Schedule> schedules) {
                Schedule schedule = schedules.get(idx-1);
                for(Apunte a: apuntes){
                    if(a.getApunteID().equals(schedule.getId())){
                        Toast.makeText(getContext(), "Found apunte!" + a.getName(), Toast.LENGTH_SHORT).show();
                        mListener.onFragmentInteraction(a);
                    }
                }
            }
        });
    }

    public static ArrayList<Schedule> getSchedules(ArrayList<Apunte> apuntes){
        ArrayList<Schedule> schedules = new ArrayList<>();
        for(Apunte a: apuntes){
            schedules.addAll(getScheduleFromApunte(a));
        }
        return schedules;
    }
    public static ArrayList<Schedule> getSchedules(Apunte apunte){
        ArrayList<Schedule> schedules = new ArrayList<>();
        schedules.addAll(getScheduleFromApunte(apunte));
        return schedules;
    }

    public static ArrayList<Schedule> getScheduleFromApunte(Apunte apunte){
        ArrayList<Schedule> scheduleArrayList = new ArrayList<>();
        for(Integer day : apunte.getDays()){
            Schedule s = new Schedule();
            s.setClassTitle(apunte.getName());
            final Pair<Integer, Integer> start = apunte.getStartTime();
            final Pair<Integer, Integer> end = apunte.getEndTime();
            s.setId(apunte.getApunteID());
            s.setStartTime(new Time(start.first, start.second));
            s.setEndTime(new Time(end.first, end.second));
//        ArrayList<Object> listOfDays = apunte.getDays();
            s.setDay(day);
            scheduleArrayList.add(s);
        }


        return scheduleArrayList;
    }

    public void addSchedule(Schedule schedule){
        timetable.add(schedule);
    }

    private ArrayList<Schedule> debugSchedule(){
        String[] names = {"Daniela Espinosa", "Pablo Brito", "David Beckham", "Tim Berners-lee"};
        Pair<Integer, Integer>[] startTimes = new Pair[]{new Pair<>(4, 30), new Pair<>(7, 30), new Pair<>(14, 50), new Pair<>(12, 0)};

        ArrayList<Schedule> schedules = new ArrayList<>();
        for (int i = 0, namesLength = names.length; i < namesLength; i++) {
            String s = names[i];
            Random random = new Random();
            /*int hour = random.nextInt(13) + 6;
            int minutes = random.nextInt(3) * 20 + 10;*/
            int day = random.nextInt(5);
            Schedule schedule = new Schedule();
            schedule.setClassTitle(s);
            schedule.setStartTime(new Time(startTimes[i].first, startTimes[i].second));
            schedule.setDay(day);
            schedule.setEndTime(startTimes[i].second >= 40 ? new Time(startTimes[i].first + 1, startTimes[i].second - 40)
                    : new Time(startTimes[i].first, startTimes[i].second + 20));
            schedules.add(schedule);
        }
        return schedules;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Apunte uri);
    }
}
