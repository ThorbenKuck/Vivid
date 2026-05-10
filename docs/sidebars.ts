import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.
 */
const sidebars: SidebarsConfig = {
    vividSidebar: [
        'introduction',
        'getting-started',
        'core-concepts',
        {
            type: 'category',
            label: 'User Guide',
            items: [
                'user-guide/features',
                'user-guide/environments',
                'user-guide/clients',
                'user-guide/settings',
            ],
        },
        'deployment',
        'sdks',
        {
            type: 'category',
            label: 'Clients',
            items: [
                'clients/concept',
                'clients/streams',
                {
                    type: 'category',
                    label: 'SDKs',
                    items: [
                        'clients/sdks/overview',
                        'clients/sdks/api',
                        'clients/sdks/java',
                        'clients/sdks/kotlin',
                        'clients/sdks/spring-boot',
                    ],
                }
            ],
        },
        {
            type: 'category',
            label: 'Security',
            items: ['security/permissions'],
        },
    ],
};

export default sidebars;
