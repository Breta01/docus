package com.bretahajek.scannerapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.bretahajek.scannerapp.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;


public class DocumentFragment extends Fragment {
    // TODO: Replace viewPager with recylerView
    private String documentFolder;

    private String[] EXTENSION_WHITELIST = new String[]{"JPG"};
    private File[] pages;

    public DocumentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        documentFolder = DocumentFragmentArgs.fromBundle(getArguments()).getDocumentFolder();
        File documentDirectory = new File(getActivity().getExternalFilesDir(null), documentFolder);

        pages = documentDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                for (String extension : EXTENSION_WHITELIST) {
                    if (extension.equals(FilenameUtils.getExtension(s).toUpperCase()))
                        return true;
                }
                return false;
            }
        });

        if (pages == null || pages.length == 0) {
            // TODO: Document was probably deleted, solve that
            Toast.makeText(
                    getActivity().getBaseContext(),
                    "Document is empty or doesn't exist.",
                    Toast.LENGTH_SHORT).show();
            Navigation.findNavController(getActivity(), R.id.fragment_container).popBackStack();
        }

        Arrays.sort(pages, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                int n1 = getNumber(f1.getName());
                int n2 = getNumber(f2.getName());
                return n1 - n2;
            }

            private int getNumber(String name) {
                int i = 0;
                try {
                    i = Integer.parseInt(name.substring(0, name.lastIndexOf('.')));
                } finally {
                    return i;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_document, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager pageView = ((ViewPager) view.findViewById(R.id.page_view));
        pageView.setOffscreenPageLimit(2);
        pageView.setAdapter(new DocumentPagerAdapter(getChildFragmentManager(), pages));

        view.findViewById(R.id.document_go_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getActivity(), R.id.fragment_container).popBackStack();
            }
        });
    }


    private class DocumentPagerAdapter extends FragmentStatePagerAdapter {

        private File[] pages;

        public DocumentPagerAdapter(@NonNull FragmentManager fm, File[] pages) {
            super(fm);
            this.pages = pages;
        }

        public DocumentPagerAdapter(@NonNull FragmentManager fm, int behavior, File[] pages) {
            super(fm, behavior);
            this.pages = pages;
        }

        @Override
        public int getCount() {
            return pages.length;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return super.getItemPosition(object);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return PageFragment.create(pages[position]);
        }
    }


}
